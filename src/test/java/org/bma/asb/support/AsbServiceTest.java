package org.bma.asb.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.bma.asb.support.queue.AsbResponseQueueManager;
import org.bma.asb.support.queue.DefaultAsbQueue;
import org.bma.asb.support.service.AsbService;
import org.bma.asb.support.service.DefaultJsonRpcRequestHandler;
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
	
	private DefaultAsbQueue queue;
	
	private DefaultAsbQueue responseQueue;

	private AsbService asbService;
	
	private DefaultJsonRpcRequestHandler requestHandler;
	
	@Mock
	public TestService testService;

	@Mock
	private AsbResponseQueueManager responseQueueManager;

	@Before
	public void setUp() {
		Mockito.reset(service);
		requestHandler = new DefaultJsonRpcRequestHandler();
		requestHandler.setService(testService);
		requestHandler.setServiceIntefaceClass(TestService.class);
		
		givenWeHaveAServiceManager();
		givenWeHaveAQueue();
		givenWeHaveAResponseQueue();
		givenWeHaveAnAsbService();
	}

	private void givenWeHaveAResponseQueue() {
		responseQueue = new DefaultAsbQueue();
		responseQueue.setPath("aResponseQueue");
		responseQueue.setServiceManager(serviceManager);
		
		when(responseQueueManager.getResponseQueue(eq("aResponseQueue"))).thenReturn(responseQueue);
	}

	@Test
	public void verifyThatQueueIsCreatedWhenOptionIsSpecified()
			throws ServiceException {
		givenWeHaveCreatedQueues("otherQueue");
		givenEmptyQueue();
		
		whenStartService();

		thenQueueIsCreated();
	}
	
	@Test
	public void verifyThatServiceMethodIsInvoedWhenRequest() throws ServiceException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue();
		
		whenStartService();
		
		thenCreateIdeaMethodIsInvoked();
	}
	
	private void givenWeHaveResultOfServiceMethodInvoke() {
		when(testService.createNewIdea(Matchers.anyString())).thenReturn(100);
	}

	@Test
	public void verifyThatResultOfMethodInvokeIsSentInMessage() throws ServiceException, IOException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue();
		givenWeHaveResultOfServiceMethodInvoke();
		
		whenStartService();
		
		thenResultIsSentInMessage();
	}
	
	@Test
	public void verifyThatResponseIsSentWithCorrelationId() throws ServiceException, IOException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue();
		givenWeHaveResultOfServiceMethodInvoke();
		
		whenStartService();
		
		ArgumentCaptor<BrokeredMessage> bmc = ArgumentCaptor.forClass(BrokeredMessage.class);
		verify(service).sendQueueMessage(Matchers.eq("aResponseQueue"), bmc.capture());
		BrokeredMessage message = bmc.getValue();
		assertThat(message.getCorrelationId(), equalTo("CorrId"));
	}
	
	@Test
	public void verifyMethodInvokeWithVoidResult() throws ServiceException, IOException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue("/notificationRequest.txt");
		
		whenStartService();
		
		thenNotificationMethodHasBeenInvoked();
		thenResponseHasText("\"result\":null");
	}
	
	@Test
	public void verifyThatIfExeptionIsThrownThenErrorIsSent() throws ServiceException, IOException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue();
		whenMethodIsInvokedThenExeption();
		
		whenStartService();
		
		thenResponseHasText("\"error\":{\"code\":0,\"message\":\"something happened\",\"data\":{\"exceptionTypeName\":\"java.lang.RuntimeException\",\"message\":\"something happened\"");
	}
	
	@Test
	public void verifyThatIfMethodHasAnnotationAndExeptionIsThrownThenErrorIsSent() throws ServiceException, IOException {
		givenWeHaveCreatedQueues("aQueue");
		givenWeHaveMessageInQueue("/notificationRequest.txt");
		whenMethod2IsInvokedThenExeption();
		
		whenStartService();
		
		thenResponseHasText("error\":{\"code\":1,\"message\":\"Error create notification\",\"data\":{\"exceptionTypeName\":\"java.lang.RuntimeException\",\"message\":\"Error create notification\"");
	}
	

	private void whenMethodIsInvokedThenExeption() {
		when(testService.createNewIdea(Matchers.anyString())).thenThrow(new RuntimeException("something happened"));
	}

	private void whenMethod2IsInvokedThenExeption() {
		Mockito.doThrow(new RuntimeException("something happened")).when(testService).notification(Matchers.anyString());
	}
	
	private void thenNotificationMethodHasBeenInvoked() {
		verify(testService).notification(eq("bar"));
	}

	private void thenResultIsSentInMessage() throws IOException, ServiceException {
		thenResponseHasText("\"result\":100");
	}
	
	public void thenResponseHasText(String text) throws ServiceException, IOException {
		ArgumentCaptor<BrokeredMessage> bmc = ArgumentCaptor.forClass(BrokeredMessage.class);
		verify(service).sendQueueMessage(Matchers.eq("aResponseQueue"), bmc.capture());
		BrokeredMessage message = bmc.getValue();
		System.out.println(message);
		assertMessageBody(message, text);
	}

	private void thenCreateIdeaMethodIsInvoked() {
		verify(testService).createNewIdea(eq("foo"));
	}

	private void givenWeHaveMessageInQueue() throws ServiceException {
		givenWeHaveMessageInQueue("/createIdeaRequest.txt");
	}
	
	private void givenWeHaveMessageInQueue(String requestFilename) throws ServiceException {
		InputStream requstIs = AsbServiceTest.class
				.getResourceAsStream(requestFilename);
		BrokeredMessage message = new BrokeredMessage(requstIs);
		message.setMessageId("MsgId");
		message.setReplyTo("aResponseQueue");
		message.setCorrelationId("CorrId");
		
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
		asbService.setResponseQueueManager(responseQueueManager);
		asbService.setRequestHandler(requestHandler);
		asbService.setStopAfter(1);
	}

	public void verifyThatIfQueueDoesNotExistThenItIsCreated() {

	}

	private void givenWeHaveAQueue() {
		queue = new DefaultAsbQueue();
		queue.setPath("aQueue");
		queue.setServiceManager(serviceManager);
		
	}
}
