package org.bma.asb.support.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.DefaultExceptionResolver;
import com.googlecode.jsonrpc4j.ExceptionResolver;

public class AsbServiceExceptionResolver implements ExceptionResolver {
	
	private ExceptionResolver realResolver = DefaultExceptionResolver.INSTANCE;
	
	public Throwable resolveException(ObjectNode response) {
		Throwable serviceEx = realResolver.resolveException(response);
		return new AsbServiceException(serviceEx);
	}

	public ExceptionResolver getRealResolver() {
		return realResolver;
	}

	public void setRealResolver(ExceptionResolver realResolver) {
		this.realResolver = realResolver;
	}

}
