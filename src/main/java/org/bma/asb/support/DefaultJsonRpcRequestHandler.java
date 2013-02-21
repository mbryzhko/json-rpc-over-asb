package org.bma.asb.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;

public class DefaultJsonRpcRequestHandler implements JsonRpcRequestHandler {
	private final static Logger LOG = LoggerFactory.getLogger(DefaultJsonRpcRequestHandler.class);

	
	private Object service;
	private Class<?> serviceIntefaceClass;
	private JsonRpcServer jsonRpcServer;
	
	public DefaultJsonRpcRequestHandler() {
		
	}

	private JsonRpcServer getJsonRpcService() {
		if (jsonRpcServer == null) {
			jsonRpcServer = new JsonRpcServer(new ObjectMapper(), service, serviceIntefaceClass);
			jsonRpcServer.setErrorResolver(JsonRpcServer.DEFAULT_ERRROR_RESOLVER);
		}
		return jsonRpcServer;
	}
	
	public void handleRequest(InputStream request, OutputStream responce) {
		try {
			LOG.debug("Handling request for service: {}", serviceIntefaceClass);
			getJsonRpcService().handle(request, responce);
		} catch (IOException e) {
			throw new AsbException("IO error during processing request for service: " + serviceIntefaceClass, e);
		} catch (RuntimeException e) {
			throw new AsbException("Unexpected error during processing request for service: " + serviceIntefaceClass, e);
		}
	}

	public Object getService() {
		return service;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public Class<?> getServiceIntefaceClass() {
		return serviceIntefaceClass;
	}

	public void setServiceIntefaceClass(Class<?> serviceIntefaceClass) {
		this.serviceIntefaceClass = serviceIntefaceClass;
	}

}
