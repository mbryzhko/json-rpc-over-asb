package org.bma.asb.support;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

@RunWith(MockitoJUnitRunner.class)
public class AsbServiceTest extends AbstractAsbTest {
	
	private AsbQueue queue;

	private AsbService asbService;
	
	private DefaultJsonRpcRequestHandler requestHandler;
	
	@Mock
	public TestService testService;

	@Before
	public void setUp() {
		Mockito.reset(service);
		requestHandler = new DefaultJsonRpcRequestHandler();
		requestHandler.setService(testService);
		requestHandler.setServiceIntefaceClass(TestService.class);
		
		givenWeHaveAServiceManager();
		givenWeHaveAQueue();
		givenWeHaveAnAsbService();
	}

	@Test
	public void verifyThatQueueIsCreatedWhenOptionIsSpecified()
			throws ServiceException {
		givenWeHaveListOfQueues("otherQueue");
		givenEmptyQueue();
		
		whenStartService();

		thenQueueIsCreated();
	}
	
	private void givenEmptyQueue() throws ServiceException {
		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(null);
		when(service.receiveQueueMessage(eq("aQueue"),
						isA(ReceiveMessageOptions.class)))
				.thenReturn(brMessage);
	}

	@Test
	public void verifyThatServiceMethodIsInvoedWhenRequest() throws ServiceException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveMessageInQueue();
		
		whenStartService();
		
		thenCreateIdeaMethodIsInvoked();
	}
	
	private void givenWeHaveResultOfServiceMethodInvoke() {
		when(testService.createNewIdea(Matchers.anyString())).thenReturn(100);
	}

	@Test
	public void verifyThatResultOfMethodInvokeIsSentInMessage() throws ServiceException, IOException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveMessageInQueue();
		givenWeHaveResultOfServiceMethodInvoke();
		
		whenStartService();
		
		thenResultIsSentInMessage();
	}
	
	@Test
	public void verifyThatReponceIsSentWithReplayToSessionId() throws ServiceException, IOException {
		givenWeHaveListOfQueues("aQueue");
		givenWeHaveMessageInQueue();
		givenWeHaveResultOfServiceMethodInvoke();
		
		whenStartService();
		
		ArgumentCaptor<BrokeredMessage> bmc = ArgumentCaptor.forClass(BrokeredMessage.class);
		verify(service).sendQueueMessage(Matchers.eq("aQueue"), bmc.capture());
		BrokeredMessage message = bmc.getValue();
		Assert.assertThat(message.getReplyToSessionId(), CoreMatchers.equalTo("200"));
	}
	

	private void thenResultIsSentInMessage() throws IOException, ServiceException {
		ArgumentCaptor<BrokeredMessage> bmc = ArgumentCaptor.forClass(BrokeredMessage.class);
		verify(service).sendQueueMessage(Matchers.eq("aQueue"), bmc.capture());
		BrokeredMessage message = bmc.getValue();
		System.out.println(message);
		assertMessageBody(message, "\"result\":100");
	}

	private void thenCreateIdeaMethodIsInvoked() {
		verify(testService).createNewIdea(eq("foo"));
	}

	private void givenWeHaveMessageInQueue() throws ServiceException {
		InputStream requstIs = AsbServiceTest.class
				.getResourceAsStream("/createIdeaRequest.txt");
		BrokeredMessage message = new BrokeredMessage(requstIs);
		message.setSessionId("200");
		message.setMessageId("MsgId");
		
		ReceiveQueueMessageResult brMessage = new ReceiveQueueMessageResult(message);
		when(service.receiveQueueMessage(eq("aQueue"),
						isA(ReceiveMessageOptions.class)))
				.thenReturn(brMessage);
	}

	private void thenQueueIsCreated() throws ServiceException {
		verify(service).createQueue(isA(QueueInfo.class));
	}

	private void whenStartService() {
		asbService.start();
	}

	private void givenWeHaveAnAsbService() {
		asbService = new AsbService();
		asbService.setQueue(queue);
		asbService.setRequestHandler(requestHandler);
		asbService.setStopAfter(1);
	}

	public void verifyThatIfQueueDoesNotExistThenItIsCreated() {

	}

	private void givenWeHaveAQueue() {
		queue = new AsbQueue("aQueue", serviceManager);
	}
}
