/*
 *   Copyright (C) 2016 to the original authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *           https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.springframework.cloud.kubernetes.discovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.kubernetes.discovery")
public class KubernetesDiscoveryProperties {

    private boolean enabled = true;

    @Value("${spring.application.name:unknown}")
    private String serviceName = "unknown";

	private int catalogServicesWatchDelay = 30000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

	public int getCatalogServicesWatchDelay() {
		return catalogServicesWatchDelay;
	}

	public void setCatalogServicesWatchDelay(int catalogServicesWatchDelay) {
		this.catalogServicesWatchDelay = catalogServicesWatchDelay;
	}
}
