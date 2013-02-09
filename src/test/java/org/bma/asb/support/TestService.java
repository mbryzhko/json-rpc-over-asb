package org.bma.asb.support;

import com.googlecode.jsonrpc4j.JsonRpcParam;

public interface TestService {
	int createNewIdea(@JsonRpcParam("name") String name);
}
