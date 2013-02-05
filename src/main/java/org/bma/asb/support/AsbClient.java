package org.bma.asb.support;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class AsbClient {
	private final static Logger LOG = LoggerFactory.getLogger(AsbClient.class);
	
	private AsbQueue queue;
	private AsbJsonRpcClient jsonRpc;
	
	public Object invoke(Method method, Object... args) {
		UUID sessionId = UUID.randomUUID();
		LOG.debug("Preparing request for method: {} within session: {}", method.getName(), sessionId);
		
		queue.assertQueueExists();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		jsonRpc.serialiseRequest(outputStream, method, args);
		byte[] jsonRpcRequest = outputStream.toByteArray();
		
		BrokeredMessage jsonRpcMessage = new BrokeredMessage(jsonRpcRequest);
		jsonRpcMessage.setSessionId(sessionId.toString());
		
		LOG.debug("Sending message to service in session: {}", sessionId);
		queue.sendRequest(jsonRpcMessage);
		return null;
	}

	public AsbQueue getQueue() {
		return queue;
	}

	public void setQueue(AsbQueue queue) {
		this.queue = queue;
	}

	public AsbJsonRpcClient getJsonRpc() {
		return jsonRpc;
	}

	public void setJsonRpc(AsbJsonRpcClient jsonRpc) {
		this.jsonRpc = jsonRpc;
	}
}
