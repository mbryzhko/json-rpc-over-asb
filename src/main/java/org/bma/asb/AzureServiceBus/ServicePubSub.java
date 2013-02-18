package org.bma.asb.AzureServiceBus;

import java.io.IOException;

import org.bma.asb.support.BrokeredMessageHelper;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusService;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMode;
import com.microsoft.windowsazure.services.serviceBus.models.RuleInfo;
import com.microsoft.windowsazure.services.serviceBus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;

public class ServicePubSub {
	public static final String REQUEST_SUB_NAME = "IdeaServiceRequestSub";
	public static final String TOPIC_NAME = "IdeaServiceTopic";
	
	public static void main(String[] args) throws ServiceException, IOException {
		String issuer = "owner";
		String key = "/YO6Q9o94KLmjp4Ce0jDwWsDnZ4Hfk7UOVnh1pP1KMc=";
		Configuration config = ServiceBusConfiguration
				.configureWithWrapAuthentication("makethingsbma", issuer, key,
						".servicebus.windows.net/",
						"-sb.accesscontrol.windows.net/WRAPv0.9");
		System.out.println("Created Configuration " + config);
		ServiceBusContract service = ServiceBusService.create(config);
		
		createTopic(service);
		
		TopicInfo topicInfo = service.getTopic(TOPIC_NAME).getValue();
		
		createRequestSub(service, REQUEST_SUB_NAME);
		
		GetSubscriptionResult subscription = service.getSubscription(TOPIC_NAME, REQUEST_SUB_NAME);
		
		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;  
		opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
		
		while (true) {
			BrokeredMessage reqMessage = service.receiveSubscriptionMessage(TOPIC_NAME, REQUEST_SUB_NAME, opts).getValue();
			if (reqMessage != null && reqMessage.getMessageId() != null) {
				System.out.println("Received request message " + BrokeredMessageHelper.messageDetails(reqMessage));
				System.out.println("Request " + BrokeredMessageHelper.readMessage(reqMessage));
				service.deleteMessage(reqMessage);
			} else {
				System.out.println("Received empty message");
			}
		}
		
	}

	public static void createRequestSub(ServiceBusContract service, String subName)
			throws ServiceException {
		
		ListSubscriptionsResult subscriptions = service.listSubscriptions(TOPIC_NAME);
		for (SubscriptionInfo si : subscriptions.getItems()) {
			if (si.getName().equalsIgnoreCase(subName)) {
				System.out.println(subName + " already exists");
				service.deleteSubscription(TOPIC_NAME, subName);
			}
		}

		System.out.println("Creating subscription " + subName);
		SubscriptionInfo subscription = new SubscriptionInfo(subName);
		subscription.setRequiresSession(true);
		service.createSubscription(TOPIC_NAME, subscription);
	}

	private static void createTopic(ServiceBusContract service) throws ServiceException {
		ListTopicsResult topics = service.listTopics();
		for (TopicInfo ti: topics.getItems()) {
			if (ti.getPath().equalsIgnoreCase(TOPIC_NAME)) {
				System.out.println(TOPIC_NAME + " already exists");
				service.deleteTopic(TOPIC_NAME);
			}
		}
		
		System.out.println("Creating topic " + TOPIC_NAME);
		TopicInfo ti = new TopicInfo(TOPIC_NAME);
		ti.setRequiresDuplicateDetection(true);
		service.createTopic(ti);
	}
}
