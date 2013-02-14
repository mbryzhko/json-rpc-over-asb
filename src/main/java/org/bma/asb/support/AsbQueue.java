package org.bma.asb.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

public class AsbQueue {
	private final static Logger LOG = LoggerFactory.getLogger(AsbQueue.class);
	
	private String path;
	private AsbServiceManager serviceManager;
	private ReceiveMessageOptions receiveMessageOptions;


	public AsbQueue(String path, AsbServiceManager serviceManager) {
		this.path = path;
		this.serviceManager = serviceManager;
		receiveMessageOptions = new ReceiveMessageOptions();
		receiveMessageOptions.setTimeout(10);
	}

	public String getPath() {
		return path;
	}

	public void assertCreated() throws AsbException {
		if (!isCreated()) {
			throw new AsbException("Queue " + getPath() + " does not exist");
		}
	}
	
	public void sendRequest(BrokeredMessage message) {
		try {
			LOG.debug("Sending message {} to queue {}", message, getPath());
			serviceManager.getService().sendQueueMessage(getPath(), message);
		} catch (ServiceException e) {
			throw new AsbException(e);
		}
	}
	
	public boolean isCreated() throws AsbException {
		try {
			LOG.debug("Checking if queue: {} exists", getPath());
			ListQueuesResult listQueues = serviceManager.getService()
					.listQueues();
			for (QueueInfo q : listQueues.getItems()) {
				if (q.getPath().equals(getPath())) {
					return true;
				}
			}
		} catch (ServiceException e) {
			throw new AsbException("Error getting list of queues", e);
		}
		return false;
	}
	
	public void create() {
		try {
			LOG.info("Creating new queue {}", getPath());
			QueueInfo info = new QueueInfo(getPath());
			info.setRequiresSession(true);
			info.setRequiresDuplicateDetection(true);
			serviceManager.getService().createQueue(info);
		} catch (ServiceException e) {
			throw new AsbException("Error creating new queue " + getPath(), e);
		}
	}
	
	public BrokeredMessage receiveMessage() {
		try {
			 ReceiveQueueMessageResult message = serviceManager.getService()
					.receiveQueueMessage(path, receiveMessageOptions);
			
			LOG.debug("Recevied message {} from gueue {}", message, getPath());
			
			return message != null ? message.getValue() : null;
		} catch (ServiceException e) {
			throw new AsbException("Error receiving message from gueue "
					+ getPath(), e);
		}
	}

}
