/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class ApplicationRegistryTest {

	private ApplicationRegistry registry = new ApplicationRegistry(new SimpleApplicationStore(),
			new HashingApplicationUrlIdGenerator());

	public ApplicationRegistryTest() {
		registry.setApplicationEventPublisher(Mockito.mock(ApplicationEventPublisher.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_null() throws Exception {
		registry.register(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_no_name() throws Exception {
		registry.register(new Application("http://localhost/health", "", "", ""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_no_healthUrl() throws Exception {
		registry.register(new Application("", "", "", "name"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_healthUrl() throws Exception {
		registry.register(new Application("not-an-url", "", "", "name"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_mgmtUrl() throws Exception {
		registry.register(new Application("http://localhost/health", "not-a-url", "",
				"name"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_svcUrl() throws Exception {
		registry.register(new Application("http://localhost/health", "", "not-a-url",
				"name"));
	}

	@Test
	public void register() throws Exception {
		Application app = registry.register(new Application(
				"http://localhost:8080/health", "", "", "abc"));

		assertEquals("http://localhost:8080/health", app.getHealthUrl());
		assertEquals("abc", app.getName());
		assertNotNull(app.getId());
	}

	@Test
	public void getApplication() throws Exception {
		Application app = registry.register(new Application(
				"http://localhost:8080/health", "http://localhost:8080/", "", "abc"));
		assertEquals(app, registry.getApplication(app.getId()));
		assertEquals("http://localhost:8080/", app.getManagementUrl());
	}

	@Test
	public void getApplications() throws Exception {
		Application app = registry.register(new Application(
				"http://localhost:8080/health", "", "", "abc"));

		Collection<Application> applications = registry.getApplications();
		assertEquals(1, applications.size());
		assertTrue(applications.contains(app));
	}

	@Test
	public void getApplicationsByName() throws Exception {
		Application app = registry.register(new Application(
				"http://localhost:8080/health", "", "", "abc"));
		Application app2 = registry.register(new Application(
				"http://localhost:8081/health", "", "", "abc"));
		Application app3 = registry.register(new Application(
				"http://localhost:8082/health", "", "", "cba"));

		Collection<Application> applications = registry.getApplicationsByName("abc");
		assertEquals(2, applications.size());
		assertTrue(applications.contains(app));
		assertTrue(applications.contains(app2));
		assertFalse(applications.contains(app3));
	}
}
