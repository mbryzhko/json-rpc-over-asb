package org.bma.asb.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class AsbRemoteServiceFactory<T> implements FactoryBean<T> {

	private Class<T> serviceClass;
	private AsbClient client;

	public T getObject() throws Exception {
		return serviceClass.cast(Proxy.newProxyInstance(
				AsbRemoteServiceFactory.class.getClassLoader(),
				new Class<?>[] { serviceClass }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						
						return client.invoke(method, args);
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
