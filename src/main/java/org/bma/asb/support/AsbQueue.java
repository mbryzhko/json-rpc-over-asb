package org.bma.asb.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

public class AsbQueue {
	private final static Logger LOG = LoggerFactory.getLogger(AsbQueue.class);
	
	private String path;
	private AsbServiceManager serviceManager;

	public AsbQueue(String path, AsbServiceManager serviceManager) {
		super();
		this.path = path;
		this.serviceManager = serviceManager;
	}

	public String getPath() {
		return path;
	}

	public void assertQueueExists() throws AsbException {
		try {
			LOG.debug("Checking if queue: {} exists", getPath());
			ListQueuesResult listQueues = serviceManager.getService()
					.listQueues();
			for (QueueInfo q : listQueues.getItems()) {
				if (q.getPath().equals(getPath())) {
					return;
				}
			}
			throw new AsbException("Queue " + getPath() + " does not exist");
		} catch (ServiceException e) {
			throw new AsbException(e);
		}
	}
	
	public void sendRequest(BrokeredMessage message) {
		try {
			serviceManager.getService().sendQueueMessage(getPath(), message);
		} catch (ServiceException e) {
			throw new AsbException(e);
		}
	}

}
