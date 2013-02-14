package org.bma.asb.support;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.mockito.Mock;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

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

	protected void assertMessageBody(BrokeredMessage message, String expectedBodyText) throws IOException {
		InputStream is = message.getBody();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String jsonRpcRequest = br.readLine();
		System.out.println(jsonRpcRequest);
		assertThat(jsonRpcRequest, CoreMatchers.containsString(expectedBodyText));
	}
	
	protected void givenEmptyQueue() throws ServiceException {
		when(service.receiveQueueMessage(eq("aQueue"),
						isA(ReceiveMessageOptions.class)))
				.thenReturn(emptyMessage());
	}

	protected ReceiveQueueMessageResult emptyMessage() {
		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(null);
		return brMessage;
	}
}
