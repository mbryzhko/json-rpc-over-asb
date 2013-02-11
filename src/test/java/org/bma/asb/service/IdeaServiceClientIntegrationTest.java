package org.bma.asb.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/ideaservice-client-context.xml")
public class IdeaServiceClientIntegrationTest {
	@Autowired
	private IdeaService ideaService;

	@Test
	public void verifyThatIntialised() {
		Assert.assertNotNull(ideaService);
	}
	
	@Test
	public void verifyRequestIsSent() {
		for (int i = 0; i < 10; i++) {
		ideaService.createNewIdea("foo");
		ideaService.createNewIdea("bar");
		ideaService.createNewIdea("baz");
		}
	}
}
