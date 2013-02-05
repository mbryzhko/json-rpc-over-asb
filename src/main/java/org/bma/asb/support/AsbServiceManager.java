package org.bma.asb.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusService;

public class AsbServiceManager {
	
	private final static Logger LOG = LoggerFactory.getLogger(AsbServiceManager.class);
	
	private String issuer;
	private String key;
	private String namespace;
	private String serviceBusRootUri;
	private String wrapRootUri;

	private ServiceBusContract service;

	public ServiceBusContract getService() {
		if (service == null) {
			LOG.debug("Creating Service Bus Contract");
			Configuration config = ServiceBusConfiguration
					.configureWithWrapAuthentication(namespace, issuer, key,
							serviceBusRootUri, wrapRootUri);
			service = ServiceBusService.create(config);
			LOG.info("Created Service Bus Contract {}", service);
		}
		return service;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getServiceBusRootUri() {
		return serviceBusRootUri;
	}

	public void setServiceBusRootUri(String serviceBusRootUri) {
		this.serviceBusRootUri = serviceBusRootUri;
	}

	public String getWrapRootUri() {
		return wrapRootUri;
	}

	public void setWrapRootUri(String wrapRootUri) {
		this.wrapRootUri = wrapRootUri;
	}

}
