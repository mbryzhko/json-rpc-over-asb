package org.bma.asb.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
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
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

@RunWith(MockitoJUnitRunner.class)
public class AsbClientTest extends AbstractAsbTest {
	
	private AsbClient client;
	
	private DefaultAsbQueue queue;
	private DefaultAsbQueue responseQueue;
	
	private DefaultAsbJsonRpcClient rpcClient;
	private TestCorrelationId correlationId = new TestCorrelationId();
	
	@Before
	public void before() {
		correlationId.nextIdShouldBeUnique();
		rpcClient = new DefaultAsbJsonRpcClient();
		givenWeHaveAServiceManager();
		givenWeHaveAQueue();
		givenWeHaveAClient();
	}

	private void givenWeHaveAClient() {
		client = new AsbClient();
		client.setQueue(queue);
		client.setResponseQueue(responseQueue);
		client.setAsbJsonRpc(rpcClient);
		client.setReponsePullTimeout(10);
		client.setCorrelationId(correlationId);
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
	@Ignore
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
		BrokeredMessage message = expectedRequestMessage();
		assertMessageBody(message, "\"method\":\"createNewIdea\",\"params\":{\"name\":\"foo\"}");
	}

	private BrokeredMessage expectedRequestMessage() throws ServiceException {
		ArgumentCaptor<BrokeredMessage> msgCap = ArgumentCaptor.forClass(BrokeredMessage.class);
		verify(service).sendQueueMessage(Matchers.eq("aQueue"), msgCap.capture());
		BrokeredMessage message = msgCap.getValue();
		return message;
	}
	
	@Test
	public void verifyThatResponseMessageDeserialisedIntoResult() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveQueueWith(reponseMessage());
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		// then
		assertThat(Integer.class.cast(result), CoreMatchers.is(100));
	
	}
	
	@Test
	public void verifyThatAClientPullsResponseFromQueue() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveQueueWith(emptyMessage(), reponseMessage());
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		// then
		assertThat(Integer.class.cast(result), CoreMatchers.is(100));
		
	}
	
	@Test
	public void verifyThatResponseQueueIsCreatedBeforeStart() throws ServiceException {
		givenWeHaveListOfQueues("aQueue");
		
		whenAClientIsInited();
		
		thenResponseQueueIsCreated();
	}
	
	@Test
	public void verifyThatRequestHasResponseQueueName() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		thenRequestMessageHasReponseQueue();
	}
	
	@Test
	public void verifyThatRequestHasUniqueCorrelationId() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		whenAClientIsInited();
		
		// when
		client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		String one = thenRequestMessageHasCorrelationId();
	
		Mockito.reset(service);
		client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		String two = thenRequestMessageHasCorrelationId();
	
		assertThat(one, CoreMatchers.not(CoreMatchers.equalTo(two)));
	}

	@Test
	public void verifyThatResponseWithWrongCorrelationIdIsIgnored() throws ServiceException, SecurityException, NoSuchMethodException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveQueueWith(reponseMessageWithWrongCorrId(), reponseMessage());
		whenAClientIsInited();
		
		// when
		Object result = client.invoke(TestService.class.getDeclaredMethod("createNewIdea", String.class), "foo");
		
		assertThat(Integer.class.cast(result), CoreMatchers.is(100));
	}
	
	private String thenRequestMessageHasCorrelationId() throws ServiceException {
		BrokeredMessage message = expectedRequestMessage();
		assertThat(message.getCorrelationId(), CoreMatchers.notNullValue());
		return message.getCorrelationId();
	}

	private void thenRequestMessageHasReponseQueue() throws ServiceException {
		BrokeredMessage message = expectedRequestMessage();
		assertThat(message.getReplyTo(), equalTo("aReponseQueue"));
	}

	private void thenResponseQueueIsCreated() throws ServiceException {
		verify(service).createQueue(isA(QueueInfo.class));
	}

	private void givenWeHaveQueueWith(ReceiveQueueMessageResult message, ReceiveQueueMessageResult... messages ) throws ServiceException {
		when(service.receiveQueueMessage(eq("aReponseQueue"), isA(ReceiveMessageOptions.class)))
		.thenReturn(message, messages);
	}

	private ReceiveQueueMessageResult reponseMessage() {
		InputStream requstIs = this.getClass().getResourceAsStream("/createIdeaResponse.txt");
		BrokeredMessage message = new BrokeredMessage(requstIs);
		message.setMessageId("MsgId");
		correlationId.setNextCorrelationId("CorrId");
		message.setCorrelationId("CorrId");

		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(message);
		return brMessage;
	}
	
	private ReceiveQueueMessageResult reponseMessageWithWrongCorrId() {
		BrokeredMessage message = new BrokeredMessage("WrongMessage");
		message.setMessageId("MsgId");
		message.setCorrelationId("WrongCorrId");

		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(message);
		return brMessage;
	}

	private void givenWeHaveAQueue() {
		queue = new DefaultAsbQueue();
		queue.setPath("aQueue");
		queue.setServiceManager(serviceManager);
		
		responseQueue = new DefaultAsbQueue();
		responseQueue.setPath("aReponseQueue");
		responseQueue.setServiceManager(serviceManager);
	}
	
	
}	
