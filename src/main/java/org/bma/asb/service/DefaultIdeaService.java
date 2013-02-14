package org.bma.asb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIdeaService implements IdeaService {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultIdeaService.class);

	private int i = 1;
	
	public int createNewIdea(String name) {
		LOG.info("Creating new idea {}", name);
		return i++;
	}

}
