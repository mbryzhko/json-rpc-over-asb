package org.bma.asb.support.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bma.asb.support.AsbServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class AsbRemoteServiceFactory<T> implements FactoryBean<T> {

	private final static Logger LOG = LoggerFactory.getLogger(AsbRemoteServiceFactory.class);
	
	private Class<T> serviceClass;
	private AsbClient client;

	public T getObject() throws Exception {
		return serviceClass.cast(Proxy.newProxyInstance(
				AsbRemoteServiceFactory.class.getClassLoader(),
				new Class<?>[] { serviceClass }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						
						Object result = client.invoke(method, args);
						LOG.debug("Result of client invocation {}", result);
						
						return result;
					}
				}));
	}

	public Class<?> getObjectType() {
		return serviceClass;
	}

	public boolean isSingleton() {
		return true;
	}

	@Required
	public void setServiceClass(Class<T> serviceClass) {
		this.serviceClass = serviceClass;
	}

	public void setClient(AsbClient client) {
		this.client = client;
	}
	
	

}
