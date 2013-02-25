package org.bma.asb.support.client;

/**
 * Holds Exception thrown by Service.
 *
 */
public class AsbServiceException extends RuntimeException {
	
	private static final long serialVersionUID = -3151186453299805844L;

	private Throwable payload;
	
	public AsbServiceException(Throwable payload) {
		this.payload = payload;
	}

	public Throwable getPayload() {
		return payload;
	}

}
