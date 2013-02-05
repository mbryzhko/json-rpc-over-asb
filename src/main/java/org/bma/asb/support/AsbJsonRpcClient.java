package org.bma.asb.support;

import java.io.OutputStream;
import java.lang.reflect.Method;

public interface AsbJsonRpcClient {
	void serialiseRequest(OutputStream outputStream, Method method, Object... args) throws AsbException;
}
