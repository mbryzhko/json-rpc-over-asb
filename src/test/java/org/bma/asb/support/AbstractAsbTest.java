package org.bma.asb.support;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.Mock;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

public abstract class AbstractAsbTest {
	@Mock
	protected AsbServiceManager serviceManager;
	
	@Mock
	protected ServiceBusContract service;

	protected void givenWeHaveAServiceManager() {
		when(serviceManager.getService()).thenReturn(service);
	}
	
	protected void givenWeHaveListOfQueues(String queueName) throws ServiceException {
		QueueInfo qi = new QueueInfo(queueName);
		ListQueuesResult listQueuesResult = new ListQueuesResult();
		listQueuesResult.setItems(Arrays.asList(qi));
		when(service.listQueues()).thenReturn(listQueuesResult);
	}
}
