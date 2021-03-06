package org.bma.asb.support.queue;

import org.bma.asb.support.AsbException;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public interface AsbQueue {

	public abstract String getPath();

	public abstract void assertCreated() throws AsbException;

	public abstract void sendMessage(BrokeredMessage message);

	public abstract boolean isCreated() throws AsbException;

	public abstract void create();

	public abstract BrokeredMessage receiveMessage();

}