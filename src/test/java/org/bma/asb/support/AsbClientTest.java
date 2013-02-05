package org.bma.asb.support;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.bma.asb.service.IdeaService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

@RunWith(MockitoJUnitRunner.class)
public class AsbClientTest {
	
	private AsbClient client;
	
	private AsbQueue queue;
	
	private DefaultAsbJsonRpcClient rpcClient;
	
	@Mock
	private AsbServiceManager serviceManager;
	
	@Mock
	private ServiceBusContract service;
	
	@Before
	public void before() {
		rpcClient = new DefaultAsbJsonRpcClient();
		givenWeHaveAServiceManager();
		givenWeHaveAQueue();
		givenWeHaveAClient();
	}

	private void givenWeHaveAClient() {
		client = new AsbClient();
		client.setQueue(queue);
		client.setJsonRpc(rpcClient);
	}
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void verifyThatIfNoQueueThenException() throws SecurityException, NoSuchMethodException, ServiceException {
		givenWeHaveListOfQueues("myQueue");
		
		expectedException.expect(AsbException.class);
		
		// when
		client.invoke(IdeaService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	}
	
	@Test
	public void verifyThatMessageIsSentToService() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		
		// when
		client.invoke(IdeaService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), Matchers.isA(BrokeredMessage.class));
	}
	
	@Test
	public void verifyThatMessageIsSentHasASession() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		
		// when
		client.invoke(IdeaService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		ArgumentCaptor<BrokeredMessage> msgCap = ArgumentCaptor.forClass(BrokeredMessage.class);
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), msgCap.capture());
		assertThat(msgCap.getValue().getSessionId(), CoreMatchers.notNullValue());
	}
	
	@Test
	public void verifyThatMessageIsSentHasJsonRpcRequest() throws ServiceException, SecurityException, NoSuchMethodException, IOException {
		givenWeHaveListOfQueues("aQueue");
		
		// when
		client.invoke(IdeaService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		ArgumentCaptor<BrokeredMessage> msgCap = ArgumentCaptor.forClass(BrokeredMessage.class);
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), msgCap.capture());
		InputStream is = msgCap.getValue().getBody();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String jsonRpcRequest = br.readLine();
		assertThat(jsonRpcRequest, CoreMatchers.containsString("\"method\":\"createNewIdea\",\"params\":{\"name\":\"foo\"}"));
	}
	
	private void givenWeHaveListOfQueues(String queueName) throws ServiceException {
		QueueInfo qi = new QueueInfo(queueName);
		ListQueuesResult listQueuesResult = new ListQueuesResult();
		listQueuesResult.setItems(Arrays.asList(qi));
		when(service.listQueues()).thenReturn(listQueuesResult);
	}

	private void givenWeHaveAQueue() {
		queue = new AsbQueue("aQueue", serviceManager);
	}
	
	private void givenWeHaveAServiceManager() {
		when(serviceManager.getService()).thenReturn(service);
	}
}	
