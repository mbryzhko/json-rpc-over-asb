package org.bma.asb.support.queue;

public class TimestampResponseQueueNaming implements ResponseQueueNaming {

	private String queueNamePrefix = "response_queue_";
	
	public String getQueueName() {
		return queueNamePrefix + System.currentTimeMillis();
	}

	public String getQueueNamePrefix() {
		return queueNamePrefix;
	}

	public void setQueueNamePrefix(String queueNamePrefix) {
		this.queueNamePrefix = queueNamePrefix;
	}

}
