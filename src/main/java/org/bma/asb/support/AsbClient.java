package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class AsbClient {
	private final static Logger LOG = LoggerFactory.getLogger(AsbClient.class);
	
	private AsbQueue queue;
	private AsbQueue responseQueue;
	private AsbJsonRpcClient absJsonRpc;
	private long reponsePullTimeout = 10000;
	private CorrelationId correlationId = new DefaultCorrelationId();
	
	public void init() {
		LOG.info("Initialising client");
		queue.assertCreated();
		
		if (!responseQueue.isCreated()) {
			responseQueue.create();
		}
		
	}
	
	public Object invoke(Method method, Object... args) {
		String corrId = correlationId.nextId();
		LOG.debug("Preparing request for method: {} with corr id: {}", method.getName(), corrId);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		absJsonRpc.serialiseRequest(outputStream, method, args);
		byte[] jsonRpcRequest = outputStream.toByteArray();
		
		BrokeredMessage jsonRpcMessage = new BrokeredMessage(jsonRpcRequest);
		jsonRpcMessage.setReplyTo(responseQueue.getPath());
		jsonRpcMessage.setCorrelationId(corrId);
		
		LOG.debug("Sending request to service for corr id: {}", corrId);
		queue.sendRequest(jsonRpcMessage);
		
		Object result = Integer.valueOf(0);
		
		BrokeredMessage message = pullResponseMessage(corrId);
		if (message != null) {
			LOG.debug("Processing response from queue {}, Details {}", queue.getPath(), BrokeredMessageHelper.messageDetails(message));
			InputStream responseIs = message.getBody();
			Class<?> methodReturnType = method.getReturnType();
			result = absJsonRpc.deserialiseReponse(responseIs, methodReturnType);
		} 
		
		return result;
	}

	private BrokeredMessage pullResponseMessage(String corrId) {
		BrokeredMessage result = null;
		long start = System.currentTimeMillis();
		while (result == null && System.currentTimeMillis() - start < reponsePullTimeout) {
			BrokeredMessage message = responseQueue.receiveMessage();
			if (message != null && message.getMessageId() != null) {
				if (corrId.equals(message.getCorrelationId())) {
					result = message;
				} else {
					LOG.debug("Received message with wrong correlation Id.");
				}
			} else {
				LOG.debug("Received empty message");
			}
		}
		
		if (result == null) {
			LOG.warn("Response pulling timeout");
		}
			
		return result;
	}

	public AsbQueue getQueue() {
		return queue;
	}

	@Required
	public void setQueue(AsbQueue queue) {
		this.queue = queue;
	}

	public AsbJsonRpcClient getAbsJsonRpc() {
		return absJsonRpc;
	}

	@Required
	public void setAsbJsonRpc(AsbJsonRpcClient jsonRpc) {
		this.absJsonRpc = jsonRpc;
	}

	public long getReponsePullTimeout() {
		return reponsePullTimeout;
	}

	public void setReponsePullTimeout(long reponsePullTimeout) {
		this.reponsePullTimeout = reponsePullTimeout;
	}

	public AsbQueue getResponseQueue() {
		return responseQueue;
	}

	@Required
	public void setResponseQueue(AsbQueue responseQueue) {
		this.responseQueue = responseQueue;
	}

	public CorrelationId getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(CorrelationId correlationId) {
		this.correlationId = correlationId;
	}
	
}
