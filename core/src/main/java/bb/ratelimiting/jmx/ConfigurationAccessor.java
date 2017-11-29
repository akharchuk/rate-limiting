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
package bb.ratelimiting.jmx;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import bb.ratelimiting.config.RateLimitingSettings;

public class ConfigurationAccessor implements ConfigurationAccessorMBean {
	Logger logger = Logger.getLogger(ConfigurationAccessor.class);

	ConfigWatcher<RateLimitingSettings> watcher;
	RateLimitingSettings currentConfig;

	private static ObjectMapper mapper = new ObjectMapper();

	public ConfigurationAccessor(ConfigWatcher<RateLimitingSettings> watcher,
			RateLimitingSettings initialConfiguration) {
		this.watcher = watcher;
		this.currentConfig = initialConfiguration;
	}

	public void updateConfiguration(RateLimitingSettings newConfig) {
		this.currentConfig = newConfig;
	}

	@Override
	public String getConfiguration() {
		try {

			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

			return writer.writeValueAsString(currentConfig);
		} catch (JsonProcessingException e) {
			logger.error("Error writing configuration to string", e);
			return e.toString();
		}
	}

	@Override
	public void setConfiguration(String s) {
		try {
			ObjectReader reader = mapper.reader(RateLimitingSettings.class);

			RateLimitingSettings newConfig = reader.readValue(s);
			this.currentConfig = newConfig;
			watcher.configChanged(newConfig);
		} catch (JsonProcessingException e) {
			logger.error("Error parsing new configuration", e);
			throw new IllegalArgumentException("Invalid configuration", e);
		} catch (IOException e) {
			logger.error("Error parsing new configuration", e);
			throw new IllegalArgumentException("Error reading configuration", e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void reloadConfiguration() {
		RateLimitingSettings newConfig = watcher.refreshConfig();
		if (newConfig != null)
			this.currentConfig = newConfig;
	}

}
