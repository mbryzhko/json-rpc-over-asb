package org.bma.asb.support;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

@RunWith(MockitoJUnitRunner.class)
public class AsbClientTest extends AbstractAsbTest {
	
	private AsbClient client;
	
	private DefaultAsbQueue queue;
	
	private DefaultAsbJsonRpcClient rpcClient;
	
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
		client.setAsbJsonRpc(rpcClient);
		client.setReponsePullTimeout(10);
	}
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void verifyThatIfNoQueueThenException() throws SecurityException, NoSuchMethodException, ServiceException {
		givenWeHaveListOfQueues("myQueue");
		
		expectedException.expect(AsbException.class);
		
		whenAClientIsInited();
	}

	private void whenAClientIsInited() {
		client.init();
	}
	
	@Test
	public void verifyThatMessageIsSentToService() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		whenAClientIsInited();
		
		// when
		client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), Matchers.isA(BrokeredMessage.class));
	}
	
	@Test
	public void verifyThatMessageIsSentHasASession() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		whenAClientIsInited();
		
		// when
		client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		ArgumentCaptor<BrokeredMessage> msgCap = ArgumentCaptor.forClass(BrokeredMessage.class);
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), msgCap.capture());
		assertThat(msgCap.getValue().getSessionId(), CoreMatchers.notNullValue());
	}
	
	@Test
	public void verifyThatMessageIsSentHasJsonRpcRequest() throws ServiceException, SecurityException, NoSuchMethodException, IOException {
		givenWeHaveListOfQueues("aQueue");
		whenAClientIsInited();
		
		// when
		client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
	
		// then
		ArgumentCaptor<BrokeredMessage> msgCap = ArgumentCaptor.forClass(BrokeredMessage.class);
		Mockito.verify(service).sendQueueMessage(Matchers.eq("aQueue"), msgCap.capture());
		BrokeredMessage message = msgCap.getValue();
		assertMessageBody(message, "\"method\":\"createNewIdea\",\"params\":{\"name\":\"foo\"}");
	}
	
	@Test
	public void verifyThatResponseMessageDeserialisedIntoResult() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveResponseMessageInAQueue();
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		// then
		assertThat(Integer.class.cast(result), CoreMatchers.is(100));
	
	}
	
	@Test
	public void verifyThatAClientPullsResponseFromQueue() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveEmptyAndReponseMessagesInAQueue();
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		// then
		assertThat(Integer.class.cast(result), CoreMatchers.is(100));
		
	}
	
	@Test
	public void verifyThatResponseQueueIsCreatedBeforeStart() throws ServiceException {
		givenWeHaveListOfQueues("aQueue");
	}

	private void givenWeHaveEmptyAndReponseMessagesInAQueue() throws ServiceException {
		when(service.receiveQueueMessage(eq("aQueue"), isA(ReceiveMessageOptions.class)))
		.thenReturn(emptyMessage(), reponseMessage());
	}
	
	private void givenWeHaveResponseMessageInAQueue() throws ServiceException {
		when(service.receiveQueueMessage(eq("aQueue"), isA(ReceiveMessageOptions.class)))
				.thenReturn(reponseMessage());
	}

	private ReceiveQueueMessageResult reponseMessage() {
		InputStream requstIs = this.getClass().getResourceAsStream("/createIdeaResponse.txt");
		BrokeredMessage message = new BrokeredMessage(requstIs);
		message.setReplyToSessionId("200");
		message.setMessageId("MsgId");

		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(message);
		return brMessage;
	}

	private void givenWeHaveAQueue() {
		queue = new DefaultAsbQueue();
		queue.setPath("aQueue");
		queue.setServiceManager(serviceManager);
	}
	
	
}	
