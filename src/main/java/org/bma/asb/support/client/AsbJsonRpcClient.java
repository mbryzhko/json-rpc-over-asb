package org.bma.asb.support.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.bma.asb.support.AsbException;

public interface AsbJsonRpcClient {
	void serialiseRequest(OutputStream outputStream, Method method, Object... args) throws AsbException;

	Object deserialiseReponse(InputStream responseIs, Class<?> methodReturnType) throws AsbException;
}
