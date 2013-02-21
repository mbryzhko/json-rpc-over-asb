package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class AsbService {
	
	private final static Logger LOG = LoggerFactory.getLogger(AsbService.class);
	
	private JsonRpcRequestHandler requestHandler;
	private AsbResponseQueueManager responseQueueManager;
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
				LOG.error("Error in service: {} loop. {}", queue.getPath(), e);
			} finally {
				decStopAfter();
			}
		}
	}
	private void processRequest(BrokeredMessage request) {
		if (request != null && request.getMessageId() != null) {
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received requst message from queue {}, Details:{}", 
						queue.getPath(), BrokeredMessageHelper.messageDetails(request));
			}
			InputStream requestIs = request.getBody();
			ByteArrayOutputStream responceOs = new ByteArrayOutputStream();
			
			requestHandler.handleRequest(requestIs, responceOs);
			
			BrokeredMessage response = new BrokeredMessage(responceOs.toByteArray());
			response.setCorrelationId(request.getCorrelationId());
			
			String responseQueueName = request.getReplyTo();
			AsbQueue reponseQueue = responseQueueManager.getResponseQueue(responseQueueName);
			LOG.debug("Sending response to queue: {} for session: {}", responseQueueName, response.getReplyToSessionId());
			reponseQueue.sendRequest(response);
			
		} else {
			LOG.trace("Reveived empty message from queue: {}", queue.getPath());
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
	
	public AsbResponseQueueManager getResponseQueueManager() {
		return responseQueueManager;
	}
	
	@Required
	public void setResponseQueueManager(AsbResponseQueueManager responseQueueManager) {
		this.responseQueueManager = responseQueueManager;
	}
	
}
