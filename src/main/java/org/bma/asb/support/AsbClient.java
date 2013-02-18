package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;

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
	
	public void init() {
		LOG.info("Initialising client");
		queue.assertCreated();
		
		if (!responseQueue.isCreated()) {
			responseQueue.create();
		}
		
	}
	
	public Object invoke(Method method, Object... args) {
		UUID corrId = UUID.randomUUID();
		LOG.debug("Preparing request for method: {} with corr id: {}", method.getName(), corrId);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		absJsonRpc.serialiseRequest(outputStream, method, args);
		byte[] jsonRpcRequest = outputStream.toByteArray();
		
		BrokeredMessage jsonRpcMessage = new BrokeredMessage(jsonRpcRequest);
		jsonRpcMessage.setReplyTo(responseQueue.getPath());
		jsonRpcMessage.setCorrelationId(corrId.toString());
		
		LOG.debug("Sending request to service for corr id: {}", corrId);
		queue.sendRequest(jsonRpcMessage);
		
		Object result = Integer.valueOf(0);
		
		BrokeredMessage message = pullResponseMessage();
		if (message != null) {
			LOG.debug("Processing response from queue {}, Details {}", queue.getPath(), BrokeredMessageHelper.messageDetails(message));
			InputStream responseIs = message.getBody();
			Class<?> methodReturnType = method.getReturnType();
			result = absJsonRpc.deserialiseReponse(responseIs, methodReturnType);
		} 
		
		return result;
	}

	private BrokeredMessage pullResponseMessage() {
		BrokeredMessage result = null;
		long start = System.currentTimeMillis();
		while (result == null && System.currentTimeMillis() - start < reponsePullTimeout) {
			BrokeredMessage message = queue.receiveMessage();
			if (message != null && message.getMessageId() != null) {
				result = message;
			} else {
				LOG.debug("Recevied empty message");
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
	
}
