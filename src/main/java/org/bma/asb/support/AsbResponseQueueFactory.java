package org.bma.asb.support;

import org.springframework.beans.factory.FactoryBean;

public class AsbResponseQueueFactory implements FactoryBean<AsbQueue> {

	public AsbQueue getObject() throws Exception {
		return null;
	}

	public Class<AsbQueue> getObjectType() {
		return AsbQueue.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
