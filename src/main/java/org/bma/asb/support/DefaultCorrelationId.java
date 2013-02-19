package org.bma.asb.support;

import java.util.UUID;

public class DefaultCorrelationId implements CorrelationId {

	public String nextId() {
		return UUID.randomUUID().toString();
	}

}
