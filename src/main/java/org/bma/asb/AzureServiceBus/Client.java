package org.bma.asb.AzureServiceBus;

import java.util.Date;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusService;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.GetQueueResult;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

/**
 * Hello world!
 * 
 */
public class Client {
	private static final String Q_NAME = "ideaservicequeue";

	public static void main(String[] args) throws ServiceException {

		String issuer = "owner";
		String key = "/YO6Q9o94KLmjp4Ce0jDwWsDnZ4Hfk7UOVnh1pP1KMc=";
		Configuration config = ServiceBusConfiguration
				.configureWithWrapAuthentication("makethingsbma", issuer, key,
						".servicebus.windows.net/",
						"-sb.accesscontrol.windows.net/WRAPv0.9");
		System.out.println("Created Configuration " + config);
		ServiceBusContract service = ServiceBusService.create(config);

		System.out.println("Created Service Bus Service" + service);

		GetQueueResult queue = service.getQueue(Q_NAME);
		System.out.println("Retrieved queue " + queue);
		if (queue != null) {
			System.out.println("Deleting queue " + queue);
			service.deleteQueue(Q_NAME);
		}
		System.out.println("Creating new queue " + Q_NAME);
		service.createQueue(new QueueInfo(Q_NAME));

		for (int i = 0; i < 5; i++) {
			BrokeredMessage message = new BrokeredMessage("sendMessageWorks");
			message.setProperty("prop", new Date().toString());
			service.sendQueueMessage(Q_NAME, message);
			System.out.println("Sent message " + message);
		}
	}
}
