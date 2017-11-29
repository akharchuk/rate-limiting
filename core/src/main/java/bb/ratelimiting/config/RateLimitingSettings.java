/*
 * Copyright 2017 BlackBerry Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bb.ratelimiting.config;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import bb.ratelimiting.config.persistence.HazelcastConfig;
import bb.ratelimiting.filters.config.BaseFilterSettings;
import bb.ratelimiting.jmx.JMXConfig;

@JsonInclude(Include.NON_EMPTY)
public class RateLimitingSettings implements Serializable {
	private static final long serialVersionUID = 9123135230644809517L;

	private Map<String, BaseFilterSettings> filterDefinitions;
	private List<LabelDefinition> labels;
	private boolean enabled = false;
	private boolean persistConfig = false;
	private Set<String> disabledForHeaders;

	@JsonProperty("hazelcast")
	private HazelcastConfig hazelcastConfig;

	@JsonProperty("jmx")
	private JMXConfig jmxConfig;

	public Set<String> getDisabledForHeaders() {
		return disabledForHeaders;
	}

	public void setDisabledForHeaders(Set<String> disabledForHeaders) {
		this.disabledForHeaders = disabledForHeaders;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isPersistConfig() {
		return persistConfig;
	}

	public Map<String, BaseFilterSettings> getFilterDefinitions() {
		return filterDefinitions;
	}

	public List<LabelDefinition> getLabels() {
		return labels;
	}

	public HazelcastConfig getHazelcastConfig() {
		return hazelcastConfig;
	}

	public JMXConfig getJmxConfig() {
		return jmxConfig;
	}
}
