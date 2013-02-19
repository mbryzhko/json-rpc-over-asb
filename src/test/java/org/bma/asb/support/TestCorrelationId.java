package org.bma.asb.support;

import java.util.UUID;

public class TestCorrelationId implements CorrelationId {

	private String lastCorrelactionId;
	private String nextCorrelationId;
	
	public String nextId() {
		if (nextCorrelationId  != null) {
			lastCorrelactionId = nextCorrelationId;
		} else {
			lastCorrelactionId = UUID.randomUUID().toString();
		}
		return lastCorrelactionId;
	}

	public String getLastCorrelactionId() {
		return lastCorrelactionId;
	}
	
	public void setNextCorrelationId(String corrId) {
		this.nextCorrelationId = corrId;
	}
	
	public void nextIdShouldBeUnique() {
		nextCorrelationId = null;
	}

}
