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
package bb.ratelimiting.filters.config;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import bb.ratelimiting.config.RateLimitingSettings;

/*
 * Stores setting for all http filters, the key is http filter id defined in web.xml
 */
@JsonInclude(Include.NON_EMPTY)
public class RateLimitingSettingsMap {
	Map<String, RateLimitingSettings> settingsMap = new TreeMap<String, RateLimitingSettings>();

	public Map<String, RateLimitingSettings> getSettings() {
		return settingsMap;
	}

	public void setSettings(Map<String, RateLimitingSettings> settingsMap) {
		if (settingsMap == null)
			throw new IllegalArgumentException("Settings map cannot be null");
		this.settingsMap = settingsMap;
	}

	/*
	 * Added for testing
	 */
	public void store(String filterId, RateLimitingSettings settings) {
		settingsMap.put(filterId, settings);
	}
}
