package org.bma.asb.support;

import org.bma.asb.support.queue.AsbQueue;

public interface AsbResponseQueueManager {

	AsbQueue getResponseQueue(String responseQueueName);

}
