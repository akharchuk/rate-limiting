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

import java.io.IOException;

import javax.servlet.FilterConfig;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import bb.ratelimiting.RateLimitingFilter;
import bb.ratelimiting.filters.config.RateLimitingSettingsMap;

public class ConfigTest {

	public static final String TEST_SINGLEWAR_CONFIG_FILE = "sample-webxml-config.json";
	public static final String TEST_MAP_CONFIG_FILE = "map-sample-config.json";
	public static final String MULTI_WAR_CONFIG_FILE = "sample-multimodule-config.json";

	public static ObjectMapper mapper = new ObjectMapper();

	public static RateLimitingSettings loadTestSettings() throws IOException {
		String raw = IOUtils.toString(ClassLoader.getSystemResourceAsStream(TEST_SINGLEWAR_CONFIG_FILE));
		ObjectReader reader = mapper.reader(RateLimitingSettings.class);
		RateLimitingSettings settings = reader.readValue(raw);
		return settings;
	}

	@Mocked
	FilterConfig filterConfig;

	// @Test
	// Used to generate test config only
	public void test() throws IOException {
		String filterName = "testFilter";
		RateLimitingSettings settings = loadTestSettings();
		RateLimitingSettingsMap map = new RateLimitingSettingsMap();
		map.store(filterName, settings);
		map.store(filterName + "2", settings);

		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
	}

	@Test
	public void loadConfigWorks() throws Exception {
		new NonStrictExpectations() {
			{
				filterConfig.getFilterName();
				result = "testFilter1";

				filterConfig.getInitParameter(ConfigurationLoader.ConfigProperty);
				result = IOUtils.toString(ClassLoader.getSystemResourceAsStream(TEST_SINGLEWAR_CONFIG_FILE));
			}
		};

		RateLimitingFilter filter = new RateLimitingFilter();
		filter.init(filterConfig);

	}

	@Test
	public void loadConfigFileWorks() throws Exception {
		new NonStrictExpectations() {
			{
				filterConfig.getFilterName();
				result = "testFilter2";

				filterConfig.getInitParameter(ConfigurationLoader.SystemConfigFileProperty);
				result = ClassLoader.getSystemResource(TEST_MAP_CONFIG_FILE).getPath();
			}
		};

		RateLimitingFilter filter = new RateLimitingFilter();
		filter.init(filterConfig);
	}

	@Test
	public void loadLargeConfigFileWorks() throws Exception {
		new NonStrictExpectations() {
			{
				filterConfig.getFilterName();
				result = "RateLimitingExample1";

				filterConfig.getInitParameter(ConfigurationLoader.SystemConfigFileProperty);
				result = ClassLoader.getSystemResource(MULTI_WAR_CONFIG_FILE).getPath();
			}
		};

		RateLimitingFilter filter = new RateLimitingFilter();
		filter.init(filterConfig);
	}
}
