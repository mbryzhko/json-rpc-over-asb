package org.bma.asb.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/client-service-context.xml")
public class IdeaServiceClientTest {
	@Autowired
	private IdeaService ideaService;

	@Test
	public void verifyThatIntialised() {
		Assert.assertNotNull(ideaService);
	}
	
	@Test
	public void verifyRequestIsSent() {
		ideaService.createNewIdea("foo");
		ideaService.createNewIdea("bar");
		ideaService.createNewIdea("baz");
	}
}
