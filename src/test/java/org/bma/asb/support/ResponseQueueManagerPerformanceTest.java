package org.bma.asb.support;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/asb-service-context.xml")
public class ResponseQueueManagerPerformanceTest {
	private final static Logger LOG = LoggerFactory.getLogger(ResponseQueueManagerPerformanceTest.class);
	
	private static final int COUNT = 100;
	@Autowired
	private AsbServiceManager serviceManager;

	private DefaultAsbResponseQueueManager queueManager;
	
	@Before
	public void setUp() {
		queueManager = new DefaultAsbResponseQueueManager();
		queueManager.setServiceManager(serviceManager);
	}
	
	@Test
	@Ignore
	public void createQueues() throws ServiceException {
		for (int i = 0; i < COUNT; i++) {
			String queueName = "TestQueue" + i;
			LOG.info("Creating {}", queueName);
			serviceManager.getService().createQueue(new QueueInfo(queueName));
		}
	}
	
	@Test
	@Ignore
	public void test() throws ServiceException {
		for (int i = 0; i < COUNT; i++) {
			String queueName = "TestQueue" + i;
			LOG.info("Getting {}", queueName);
			queueManager.getResponseQueue(queueName);
		}
	}
	
	@Test
	@Ignore
	public void deleteQueues() throws ServiceException {
		for (int i = 0; i < COUNT; i++) {
			String queueName = "TestQueue" + i;
			LOG.info("Deleting {}", queueName);
			serviceManager.getService().deleteQueue(queueName);
		}
	}
}
