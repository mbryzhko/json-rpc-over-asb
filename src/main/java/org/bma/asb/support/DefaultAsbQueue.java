package org.bma.asb.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.GetQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

public class DefaultAsbQueue implements AsbQueue {
	private final static Logger LOG = LoggerFactory.getLogger(DefaultAsbQueue.class);
	
	private String path;
	private AsbServiceManager serviceManager;
	private ReceiveMessageOptions receiveMessageOptions;

	public DefaultAsbQueue() {
		receiveMessageOptions = new ReceiveMessageOptions();
		receiveMessageOptions.setTimeout(10);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
	
	public boolean isCreatedOld() throws AsbException {
		try {
			LOG.debug("Checking if queue: {} exists", getPath());
			ListQueuesResult listQueues = serviceManager.getService()
					.listQueues();
			for (QueueInfo q : listQueues.getItems()) {
				LOG.debug("Asserting queue {} against {}", getPath(), q.getPath());
				if (q.getPath().equalsIgnoreCase(getPath())) {
					return true;
				}
			}
		} catch (ServiceException e) {
			throw new AsbException("Error getting list of queues", e);
		}
		return false;
	}
	
	public boolean isCreated() throws AsbException {
		boolean result = false;
		try {
			LOG.debug("Checking if queue: {} exists", getPath());
			GetQueueResult queue = serviceManager.getService().getQueue(getPath());
			if (queue != null && queue.getValue() != null && queue.getValue().getPath().equals(getPath())) {
				result = true;
			} 
		} catch (ServiceException e) {
			LOG.error("Error checking queue {}, message {}", getPath(), e.getMessage());
		}
		return result;
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
			
			LOG.trace("Recevied message {} from gueue {}", message, getPath());
			
			return message != null ? message.getValue() : null;
		} catch (ServiceException e) {
			throw new AsbException("Error receiving message from gueue "
					+ getPath(), e);
		}
	}

	public AsbServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(AsbServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}
	
	

}
