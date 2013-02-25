package org.bma.asb.support.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.bma.asb.support.AsbException;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;

public class DefaultAsbJsonRpcClient implements AsbJsonRpcClient {

	private JsonRpcClient jsonRpcClient;
	private boolean useNamedParams = true;

	public DefaultAsbJsonRpcClient() {
		jsonRpcClient = new JsonRpcClient();
		jsonRpcClient.setExceptionResolver(new AsbServiceExceptionResolver());
	}

	public void serialiseRequest(OutputStream outputStream, Method method,
			Object... args) throws AsbException {
		Object arguments = ReflectionUtil.parseArguments(method, args,
				useNamedParams);
		try {
			jsonRpcClient.invoke(method.getName(), arguments, outputStream);
		} catch (IOException e) {
			throw new AsbException(e);
		}
	}

	public Object deserialiseReponse(InputStream responseIs, Class<?> methodReturnType) throws Throwable {
		try {
			return jsonRpcClient.readResponse(methodReturnType, responseIs);
		} catch (AsbServiceException e) {
			throw e.getPayload();
		} catch (Throwable e) {
			throw new AsbException("Error read reponse", e);
		}
	}

}
