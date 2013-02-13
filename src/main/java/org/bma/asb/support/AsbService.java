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
	private volatile boolean running = false;
	/** Stop service after specified count of messages received. */
	private volatile int stopAfter = -1;
	
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
		if (!running) {
			LOG.info("Starting Asb Service: {} instance", queue.getPath());
			if (!queue.isCreated()) {
				queue.create();
			}
			running = true;
			startReceiving();
		}
	}
	
	protected void startReceiving() {
		while (running && stopAfter != 0) {
			try {
				BrokeredMessage request = queue.receiveMessage();
				processRequest(request);
			} catch (AsbException e) {
				// TODO: introduce correct error handling
				LOG.error("Error in service: {} loop", queue.getPath());
			} finally {
				decStopAfter();
			}
		}
	}
	private void processRequest(BrokeredMessage request) {
		if (request != null && request.getMessageId() != null) {
			
			LOG.debug("Received requst message from queue: {} for session: {}", queue.getPath(), request.getSessionId());
			InputStream requestIs = request.getBody();
			ByteArrayOutputStream responceOs = new ByteArrayOutputStream();
			
			requestHandler.handleRequest(requestIs, responceOs);
			
			BrokeredMessage response = new BrokeredMessage(responceOs.toByteArray());
			response.setReplyToSessionId(request.getSessionId());
			
			LOG.debug("Sending response to queue: {} for session: {}", queue.getPath(), response.getReplyToSessionId());
			queue.sendRequest(response);
			
		} else {
			LOG.debug("Reveived empty message from queue: {}", queue.getPath());
		}
	}
	
	private void decStopAfter() {
		if (stopAfter > 0) {
			stopAfter--;
		}
	}
	
	public void stop() {
		LOG.info("Stop Asb service: ", queue.getPath());
		running = false;
	}
	
	public int getStopAfter() {
		return stopAfter;
	}
	
	public void setStopAfter(int stopAfter) {
		this.stopAfter = stopAfter;
	}
}
