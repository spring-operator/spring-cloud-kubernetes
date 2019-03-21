/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.kubernetes.config;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.kubernetes.config.example.App;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.Assert.assertEquals;

/**
 * @author Charles Moulliard
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class, properties = {
		"spring.application.name=configmap-example",
		"spring.cloud.kubernetes.reload.enabled=false" })
@AutoConfigureWebTestClient
public class ConfigMapsSpringBootTest {

	@ClassRule
	public static KubernetesServer server = new KubernetesServer();

	private static KubernetesClient mockClient;

	@Autowired(required = false)
	private Config config;

	private static final String APPLICATION_NAME = "configmap-example";

	@Autowired
	private WebTestClient webClient;

	@BeforeClass
	public static void setUpBeforeClass() {
		mockClient = server.getClient();

		// Configure the kubernetes master url to point to the mock server
		System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY,
				mockClient.getConfiguration().getMasterUrl());
		System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
		System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
		System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY,
				"false");
		System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "test");

		HashMap<String, String> data = new HashMap<>();
		data.put("bean.greeting", "Hello ConfigMap, %s!");
		server.expect().withPath("/api/v1/namespaces/test/configmaps/" + APPLICATION_NAME)
				.andReturn(200, new ConfigMapBuilder().withNewMetadata()
						.withName(APPLICATION_NAME).endMetadata().addToData(data).build())
				.always();
	}

	@Test
	public void testConfig() {
		assertEquals(config.getMasterUrl(), mockClient.getConfiguration().getMasterUrl());
		assertEquals(config.getNamespace(), mockClient.getNamespace());
	}

	@Test
	public void testConfigMap() {
		ConfigMap configmap = mockClient.configMaps().inNamespace("test")
				.withName(APPLICATION_NAME).get();
		HashMap<String, String> keys = (HashMap<String, String>) configmap.getData();
		assertEquals(keys.get("bean.greeting"), "Hello ConfigMap, %s!");
	}

	@Test
	public void testGreetingEndpoint() {
		this.webClient.get().uri("/api/greeting").exchange().expectStatus().isOk()
			.expectBody().jsonPath("content").isEqualTo("Hello ConfigMap, World!");
	}

}
