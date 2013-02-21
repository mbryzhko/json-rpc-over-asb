package org.bma.asb.support.queue;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.bma.asb.support.AsbServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class DefaultAsbResponseQueueManager implements AsbResponseQueueManager {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultAsbResponseQueueManager.class);
	
	private AsbServiceManager serviceManager;
	
	private LinkedHashMap<String, AsbQueue> queues;
	
	public DefaultAsbResponseQueueManager() {
		queues = new LinkedHashMap<String, AsbQueue>();
	}
	
	public AsbQueue getResponseQueue(String responseQueueName) {
		AsbQueue queue;
		if (queues.containsKey(responseQueueName)) {
			LOG.debug("Queue {} exists, putting on top", responseQueueName);
			queue = queues.remove(responseQueueName);
		} else {
			LOG.debug("Instantiating queue {}", responseQueueName);
			DefaultAsbQueue newQueue = new DefaultAsbQueue();
			newQueue.setPath(responseQueueName);
			newQueue.setServiceManager(serviceManager);
			newQueue.assertCreated();
			queue = newQueue;
		}
		queues.put(responseQueueName, queue);
		return queue;
	}

	public AsbServiceManager getServiceManager() {
		return serviceManager;
	}

	@Required
	public void setServiceManager(AsbServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}
	
}
