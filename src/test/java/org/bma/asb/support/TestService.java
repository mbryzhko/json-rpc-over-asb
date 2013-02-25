package org.bma.asb.support;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;

public interface TestService {
	Integer createNewIdea(@JsonRpcParam("name") String name) throws RuntimeException;

	@JsonRpcErrors(value = { @JsonRpcError(code = 1, exception = RuntimeException.class, message = "Error create notification") })
	void notification(@JsonRpcParam("action") String action) throws RuntimeException;
}
