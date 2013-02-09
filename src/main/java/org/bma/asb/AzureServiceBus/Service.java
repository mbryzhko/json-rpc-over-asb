package org.bma.asb.AzureServiceBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusService;
import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMode;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveQueueMessageResult;

public class Service {
	
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
		
		ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		opts.setReceiveMode(ReceiveMode.RECEIVE_AND_DELETE);
		while(true)
		{ 
		     ReceiveQueueMessageResult resultQM = 
		        service.receiveQueueMessage("ideaservicequeue", opts);
		     BrokeredMessage message = resultQM.getValue(); 
		     if (message != null && message.getMessageId() != null)
		     {
		        try 
		        {
		           System.out.println("Body: " + readMessageBody(message));
		           System.out.println("MessageID: " + message.getMessageId());
		           System.out.println("Custom Property: " + 
		                message.getProperty("prop"));
		           // Remove message from queue
		           System.out.println("Deleting this message.");
		           //service.deleteMessage(message);
		        }
		        catch (Exception ex)
		        {
		           // Indicate a problem, unlock message in queue
		           System.err.println("Inner exception encountered!");
		           ex.printStackTrace();
		           //service.unlockMessage(message);
		        }
		     }
		     else
		     {
		    	 
		        System.out.println("Finishing up - no more messages.");
		        //break; 
		        // Added to handle no more messages in the queue.
		        // Could instead wait for more messages to be added.
		     }
		}
	}
	
	public static String readMessageBody(BrokeredMessage msg) throws IOException {
		InputStream is = msg.getBody();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String messageBody = br.readLine();
		return messageBody;
	}
}
