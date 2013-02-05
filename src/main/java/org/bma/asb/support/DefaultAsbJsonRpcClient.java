package org.bma.asb.support;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.googlecode.jsonrpc4j.ReflectionUtil;

public class DefaultAsbJsonRpcClient implements AsbJsonRpcClient {

	private JsonRpcClient jsonRpcClient;
	private boolean useNamedParams = true;

	public DefaultAsbJsonRpcClient() {
		jsonRpcClient = new JsonRpcClient();
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

}
