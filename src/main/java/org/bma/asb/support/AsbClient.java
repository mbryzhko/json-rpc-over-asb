package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class AsbClient {
	private final static Logger LOG = LoggerFactory.getLogger(AsbClient.class);
	
	private AsbQueue queue;
	private AsbJsonRpcClient absJsonRpc;
	private long reponsePullTimeout = 10000;
	
	public Object invoke(Method method, Object... args) {
		UUID sessionId = UUID.randomUUID();
		LOG.debug("Preparing request for method: {} within session: {}", method.getName(), sessionId);
		
		queue.assertCreated();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		absJsonRpc.serialiseRequest(outputStream, method, args);
		byte[] jsonRpcRequest = outputStream.toByteArray();
		
		BrokeredMessage jsonRpcMessage = new BrokeredMessage(jsonRpcRequest);
		jsonRpcMessage.setSessionId(sessionId.toString());
		
		LOG.debug("Sending request to service in session: {}", sessionId);
		queue.sendRequest(jsonRpcMessage);
		
		Object result = Integer.valueOf(0);
		
		BrokeredMessage message = pullResponseMessage();
		if (message != null) {
			LOG.debug("Processing reponse for session {}", sessionId);
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

	public void setQueue(AsbQueue queue) {
		this.queue = queue;
	}

	public AsbJsonRpcClient getAbsJsonRpc() {
		return absJsonRpc;
	}

	public void setAsbJsonRpc(AsbJsonRpcClient jsonRpc) {
		this.absJsonRpc = jsonRpc;
	}

	public long getReponsePullTimeout() {
		return reponsePullTimeout;
	}

	public void setReponsePullTimeout(long reponsePullTimeout) {
		this.reponsePullTimeout = reponsePullTimeout;
	}
	
	
}
