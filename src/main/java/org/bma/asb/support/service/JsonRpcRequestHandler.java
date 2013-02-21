package org.bma.asb.support.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface JsonRpcRequestHandler {
	void handleRequest(InputStream request, OutputStream responce);
}
