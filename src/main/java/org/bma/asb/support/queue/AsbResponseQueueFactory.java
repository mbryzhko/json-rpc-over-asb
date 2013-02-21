package org.bma.asb.support.queue;

import org.bma.asb.support.AsbServiceManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class AsbResponseQueueFactory implements FactoryBean<AsbQueue> {

	private ResponseQueueNaming responseQueueNaming;
	private AsbServiceManager serviceManager;
	
	public AsbQueue getObject() throws Exception {
		DefaultAsbQueue queue = new DefaultAsbQueue();
		queue.setPath(responseQueueNaming.getQueueName());
		queue.setServiceManager(serviceManager);
		return queue;
	}

	public Class<AsbQueue> getObjectType() {
		return AsbQueue.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public ResponseQueueNaming getResponseQueueNaming() {
		return responseQueueNaming;
	}

	@Required
	public void setResponseQueueNaming(ResponseQueueNaming responseQueueNaming) {
		this.responseQueueNaming = responseQueueNaming;
	}

	public AsbServiceManager getServiceManager() {
		return serviceManager;
	}

	@Required
	public void setServiceManager(AsbServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}
	
}
