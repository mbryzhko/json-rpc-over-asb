package org.bma.asb.support;

import com.googlecode.jsonrpc4j.JsonRpcParam;

public interface TestService {
	Integer createNewIdea(@JsonRpcParam("name") String name);
	void notification(@JsonRpcParam("action") String action);
}
