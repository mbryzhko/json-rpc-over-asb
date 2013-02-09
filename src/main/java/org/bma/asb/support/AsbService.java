package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class AsbService {
	
	private final static Logger LOG = LoggerFactory.getLogger(AsbService.class);
	
	private JsonRpcRequestHandler requestHandler;
	private AsbQueue queue;
	
	public JsonRpcRequestHandler getRequestHandler() {
		return requestHandler;
	}
	public void setRequestHandler(JsonRpcRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
	public AsbQueue getQueue() {
		return queue;
	}
	public void setQueue(AsbQueue queue) {
		this.queue = queue;
	}
	
	public void start() {
		LOG.info("Starting Asb Service instance");
		if (!queue.isCreated()) {
			queue.create();
		}
		startReceiving();
	}
	
	protected void startReceiving() {
		BrokeredMessage message = queue.receiveMessage();
		if (message != null) {
			InputStream request = message.getBody();
			ByteArrayOutputStream responceOs = new ByteArrayOutputStream();
			requestHandler.handleRequest(request, responceOs);
		}
	}
	public void stop() {
		
	}
}
