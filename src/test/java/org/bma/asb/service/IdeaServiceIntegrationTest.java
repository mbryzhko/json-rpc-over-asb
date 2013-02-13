package org.bma.asb.service;

import org.bma.asb.support.AsbService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/ideaservice-service-context.xml")
public class IdeaServiceIntegrationTest {
	
	@Autowired
	private AsbService asbService;
	
	@Test
	public void startService() {
		asbService.start();
	}
}
