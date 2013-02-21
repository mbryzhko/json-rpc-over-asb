package org.bma.asb.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.microsoft.windowsazure.services.serviceBus.models.BrokeredMessage;

public class BrokeredMessageHelper {
	public static String messageDetails(BrokeredMessage msg) {
		StringBuilder result = new StringBuilder();
		result.append("\n").append("Message Id: ").append(msg.getMessageId()).append("\n");
		//result.append("Session Id: ").append(msg.getSessionId()).append("\n");
		//result.append("Reply To Session Id: ").append(msg.getReplyToSessionId()).append("\n");
		result.append("Correlation Id: ").append(msg.getCorrelationId()).append("\n");
		result.append("Reply To: ").append(msg.getReplyTo()).append("\n");
		//result.append("To: ").append(msg.getTo());
		return result.toString();
	}
	
	public static String readMessage(BrokeredMessage message) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(message.getBody()));
		return br.readLine();
	}
}
