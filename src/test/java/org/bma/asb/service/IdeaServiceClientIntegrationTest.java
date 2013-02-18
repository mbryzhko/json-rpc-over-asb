package org.bma.asb.service;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
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
	@Ignore
	public void verifyThatIntialised() {
		Assert.assertNotNull(ideaService);
	}
	
	@Test
	public void verifyRequestIsSent() {
		for (int i = 0; i < 200; i++) {
			int ideaId = ideaService.createNewIdea("idea-" + i);
			Assert.assertThat(ideaId > 0, CoreMatchers.is(true));
		}
	}
	
	@Test
	@Ignore
	public void verifySingleRequest() {
		int ideaId = ideaService.createNewIdea("foo");
		Assert.assertThat(ideaId > 0, CoreMatchers.is(true));
	}
}
