package org.bma.asb.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.bma.asb.support.queue.AsbQueue;
import org.bma.asb.support.queue.DefaultAsbResponseQueueManager;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import com.microsoft.windowsazure.services.core.ServiceException;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsbResponseQueueManagerTest extends AbstractAsbTest {
	
	private DefaultAsbResponseQueueManager tested;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setUp() {
		givenWeHaveAServiceManager();
		tested = new DefaultAsbResponseQueueManager();
		tested.setServiceManager(serviceManager);
	}
	
	@Test
	public void verifyThatQueueInstanceIsCreated() throws ServiceException {
		givenWeHaveCreatedQueues("newQueue");
		AsbQueue queue = tested.getResponseQueue("newQueue");
		
		assertThat(queue, notNullValue());
		assertThat(queue.getPath(), equalTo("newQueue"));
	}
	
	@Test
	public void verifyThatWhenQueueIsFirstCreatedThenExistanceIsChecked() throws ServiceException {
		givenWeHaveCreatedQueues("newQueue");
		
		AsbQueue queue = tested.getResponseQueue("newQueue");
		
		thenQueueExistanceIsAsserted();
	}
	
	@Test
	public void verifyThatWhenQueueReturedFromCache() throws ServiceException {
		givenWeHaveCreatedQueues("newQueue");
		
		AsbQueue queue1 = tested.getResponseQueue("newQueue");		
		thenQueueExistanceIsAsserted();

		AsbQueue queue = tested.getResponseQueue("newQueue");
		assertThat(queue, CoreMatchers.sameInstance(queue1));
	}
	
	@Test
	public void verifyThatExeptionIsThrownWhenQueueDoesNotExists() throws ServiceException {
		givenWeHaveCreatedQueues("someQueue");
		
		exception.expect(AsbException.class);
		
		tested.getResponseQueue("newQueue");		
	}
	
	@Test
	public void verify20KClients() {
		// TODO
	}

	private void thenQueueExistanceIsAsserted() throws ServiceException {
		verify(service).getQueue(Matchers.eq("newQueue"));
	}
}
