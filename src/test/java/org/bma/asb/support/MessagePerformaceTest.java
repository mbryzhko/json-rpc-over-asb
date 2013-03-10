	package org.bma.asb.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

public class MessagePerformaceTest {
	private String queueName;
	
	private Set<String> sentMessages;
	private Set<String> receivedMessages;
	private BlockingQueue<String> messagesToSend;
	private AtomicInteger sendersWhichStillRunning = new AtomicInteger(0);
	
	private static final int SIZE = 100;

	private List<SqsClient> senders;

	private ArrayList<SqsClient> receivers;
	
	
	@Before
	public void setUp() {
		sentMessages = new HashSet<String>();
		receivedMessages = new HashSet<String>();
		messagesToSend = new ArrayBlockingQueue<String>(SIZE);
	
		givenWeHaveAListOfMessages();
		
		messagesToSend.addAll(sentMessages);
	}
	
	private void givenWeHaveAListOfMessages() {
		System.out.println("Creating a list of " + SIZE + " messages");
		for (int i = 0; i < SIZE; i++) {
			sentMessages.add("test_message_" + System.nanoTime());
		}
	}

	@After
	public void tearDown() {
		
	}
	
	@Test
	public void verifyPerfomance() throws InterruptedException, IOException, ExecutionException {
		givenWeHaveAQueueName();
		givenWeHaveWorkers();

		ExecutorService pool = Executors.newCachedThreadPool();
		whenSendMessages(pool);
		whenReceiveMessages(pool);
		
		thenReceivedAreSameAsSent();
	}

	private void thenReceivedAreSameAsSent() {
		for (SqsClient sqsClient : receivers) {
			if (sqsClient.isReceive()) {
				receivedMessages.addAll(sqsClient.getReceived());
			}
		}
		assertThat(receivedMessages.size(), is(sentMessages.size()));
		receivedMessages.removeAll(sentMessages);
		assertThat(receivedMessages.size(), is(0));
	}

	private void whenReceiveMessages(ExecutorService pool)
			throws InterruptedException, ExecutionException {
		long duration = System.currentTimeMillis();
		List<Future<Long>> resultsReceivers = pool.invokeAll(receivers);
		duration = System.currentTimeMillis() - duration;
		int sum = 0;
		for (Future<Long> future : resultsReceivers) {
			sum += future.get();
		}
		System.out.println("Receiver: duration: " + duration + ", total spent: " + sum + ", avg: " + sum / SIZE);
	}

	private void whenSendMessages(ExecutorService pool)
			throws InterruptedException, ExecutionException {
		long duration = System.currentTimeMillis();
		List<Future<Long>> resultSenders = pool.invokeAll(senders);
		duration = System.currentTimeMillis() - duration;
		
		long sum = 0;
		for (Future<Long> future : resultSenders) {
			sum += future.get();
		}
		System.out.println("Senders: duration: " + duration + ", total spent: " + sum + ", avg: " + sum / SIZE);
	}

	private void givenWeHaveWorkers() throws IOException {
		senders = new ArrayList<SqsClient>();
		senders.add(new SqsClient(false));
//		senders.add(new SqsClient(false));
//		senders.add(new SqsClient(false));
		
		receivers = new ArrayList<SqsClient>();
		receivers.add(new SqsClient(true));
//		receivers.add(new SqsClient(true));
//		receivers.add(new SqsClient(true));
		//receivers.add(new SqsClient(true));
	}
	
	private void givenWeHaveAQueueName() {
		queueName = "test_queue_" + System.currentTimeMillis();
	}

	public class SqsClient implements Callable<Long> {
		private boolean receive;
		private String queueUrl;
		private List<String> receivedMessages;
		private long startTime;
		private ServiceBusContract asbService;
		
		public SqsClient(boolean receive) throws IOException {
			this.receive = receive;
			if (!receive) {
				sendersWhichStillRunning.incrementAndGet();
			}
			receivedMessages = new LinkedList<String>();
			createAsbService();
			createQueueIfNo();
		}

		private void createQueueIfNo() {
			try {
				System.out.println("Creating queue: " + queueName);
				QueueInfo value = asbService.createQueue(new QueueInfo(queueName)).getValue();
			} catch(ServiceException e) {
				// ok
			}
		}

		private void createAsbService() throws IOException {
			AsbServiceManager manager = new AsbServiceManager();
			
			Properties props = new Properties();
			props.load(MessagePerformaceTest.class.getResourceAsStream("/asb.properties"));
			
			manager.setIssuer(props.getProperty("idea.service.asb.issuer"));
			manager.setKey(props.getProperty("idea.service.asb.key"));
			manager.setNamespace(props.getProperty("idea.service.asb.namespace"));
			manager.setServiceBusRootUri(props.getProperty("idea.service.asb.serviceBusRootUri"));
			manager.setWrapRootUri(props.getProperty("idea.service.asb.wrapRootUri"));

			asbService = manager.getService();
		}


		
		public Long call() throws Exception {
			startTime = System.currentTimeMillis();
			if (receive) {
				System.out.println("Start Receiving");
				BrokeredMessage receivedMessage = null;
				while (true) {
					receivedMessage = asbService.receiveMessage(queueName).getValue();
					if (receivedMessage != null && receivedMessage.getMessageId() != null) {
						registerReceivedMessage(receivedMessage);
					} else {
						System.out.println("Received empty messsage");
						break;
					}
				}
				System.out.println("Stop reading");
			} 
			else {
				System.out.println("Start Sending");
				String nextMessageText = null;
				while((nextMessageText = messagesToSend.poll()) != null) {
					//System.out.println("Sending message: " + nextMessageText);
					sendMessage(nextMessageText);
				}
				sendersWhichStillRunning.decrementAndGet();
			}
			return System.currentTimeMillis() - startTime;
		}

		private void sendMessage(String textMessage) throws ServiceException {
			asbService.sendMessage(queueName, new BrokeredMessage(textMessage));
		}

		private void registerReceivedMessage(BrokeredMessage message) throws IOException {
			receivedMessages.add(BrokeredMessageHelper.readMessage(message));
			//System.out.println("Received message: " + message.getBody());
		}

		public boolean isReceive() {
			return receive;
		}

		public List<String> getReceived() {
			return receivedMessages;
		}
	}
}
