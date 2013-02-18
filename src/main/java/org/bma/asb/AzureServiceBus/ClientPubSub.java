package org.bma.asb.AzureServiceBus;

import java.util.UUID;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusService;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.GetSubscriptionResult;
import com.microsoft.windowsazure.services.serviceBus.models.GetTopicResult;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveSubscriptionMessageResult;

public class ClientPubSub {
	
	
	private static final String RESP_SUB = "IdeaServiceResponseSub";

	public static void main(String[] args) throws ServiceException {
		
		UUID sessionId = UUID.randomUUID();
		
		String issuer = "owner";
		String key = "/YO6Q9o94KLmjp4Ce0jDwWsDnZ4Hfk7UOVnh1pP1KMc=";
		Configuration config = ServiceBusConfiguration
				.configureWithWrapAuthentication("makethingsbma", issuer, key,
						".servicebus.windows.net/",
						"-sb.accesscontrol.windows.net/WRAPv0.9");
		System.out.println("Created Configuration " + config);
		ServiceBusContract service = ServiceBusService.create(config);
		
		
		GetTopicResult topic = service.getTopic(ServicePubSub.TOPIC_NAME);
		
		ServicePubSub.createRequestSub(service, RESP_SUB);
		
		GetSubscriptionResult subscription = service.getSubscription(ServicePubSub.TOPIC_NAME, RESP_SUB);
	
		for (int i = 0; i < 10; i++) {
		
		String req = "Test" + i;
		System.out.println("Sending request");
		BrokeredMessage reqMessage = new BrokeredMessage(req);
		reqMessage.setSessionId(sessionId.toString());
		
		service.sendTopicMessage(ServicePubSub.TOPIC_NAME, reqMessage);
		
		//System.out.println("Receiving response");
		//BrokeredMessage respMessage = service.receiveSubscriptionMessage(ServicePubSub.TOPIC_NAME, RESP_SUB).getValue();
	
		}
	}
}
