package org.bma.asb.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.bma.asb.support.client.AsbClient;
import org.bma.asb.support.client.AsbRemoteServiceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsbRemoteServiceFactoryTest {
	
	@Mock
	private AsbClient client;
	private AsbRemoteServiceFactory<TestService> factory;
	
	@Test
	public void verifyThatClientIsInvokedViaProxy() throws Throwable {
		givenWeHaveAFactory();
		givenResultOfInvocation(100);
		
		TestService proxy = factory.getObject();
		proxy.createNewIdea("foo");
		
		thenClientWasInvokedWithMethod("createNewIdea");
	}
	
	@Test
	public void verifyThatResultOfInvocationIsRetured() throws Throwable {
		givenWeHaveAFactory();
		givenResultOfInvocation(100);
		
		TestService proxy = factory.getObject();
		Integer result = proxy.createNewIdea("foo");
		
		assertThat(result, is(100));
	}

	private void thenClientWasInvokedWithMethod(String methodName) throws AsbException, Throwable {
		ArgumentCaptor<Method> methodCapt = ArgumentCaptor.forClass(Method.class);
		verify(client).invoke(methodCapt.capture(), any());
		Method method = methodCapt.getValue();
		assertThat(method.getName(), equalTo(methodName));
	}

	private void givenResultOfInvocation(Object result) throws AsbException, Throwable {
		when(client.invoke(isA(Method.class), any())).thenReturn(result);
	}

	private void givenWeHaveAFactory() {
		factory = new AsbRemoteServiceFactory<TestService>();
		factory.setClient(client);
		factory.setServiceClass(TestService.class);
	}
}
