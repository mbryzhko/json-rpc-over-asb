package org.bma.asb.service;

import com.googlecode.jsonrpc4j.JsonRpcParam;

public interface IdeaService {
	int createNewIdea(@JsonRpcParam("name") String name);
}
