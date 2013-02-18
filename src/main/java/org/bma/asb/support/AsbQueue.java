package org.bma.asb.support;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public interface AsbQueue {

	public abstract String getPath();

	public abstract void assertCreated() throws AsbException;

	public abstract void sendRequest(BrokeredMessage message);

	public abstract boolean isCreated() throws AsbException;

	public abstract void create();

	public abstract BrokeredMessage receiveMessage();

}