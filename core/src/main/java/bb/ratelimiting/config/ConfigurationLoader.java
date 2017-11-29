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

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterConfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import bb.ratelimiting.filters.config.RateLimitingSettingsMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class ConfigurationLoader {

	public static final String ConfigProperty = "config";
	public static final String SystemConfigFileProperty = "ratelimit.config.file";

	private static final Logger logger = Logger.getLogger(ConfigurationLoader.class);

	private static ObjectMapper mapper = new ObjectMapper();

	private ConfigLoadResult result;
	private String filterId;

	public ConfigurationLoader(String filterId) {
		this.filterId = filterId;
	}

	public ConfigLoadResult loadConfig(FilterConfig filterConfig) throws JsonProcessingException, IOException {
		if (result == null) {
			result = new ConfigLoadResult();
			result.settingsFile = findConfigFile(filterConfig);

			if (result.settingsFile != null) {
				result.settings = loadConfigFromFile(result.settingsFile, filterId);
			}

			if (result.settings == null) {
				result.settingsFile = null;
				result.settings = readConfigFromFilter(filterConfig);
			}
		}

		return result;
	}

	public void reloadConfig() throws IOException {
		if (result.settingsFile != null)
			result.settings = loadConfigFromFile(result.settingsFile, filterId);
		else
			logger.warn("Cannot reload config, as it is not stored in a file");
	}

	private RateLimitingSettings readConfigFromFilter(FilterConfig filterConfig)
			throws JsonProcessingException, IOException {
		ObjectReader reader = mapper.reader(RateLimitingSettings.class);

		if (filterConfig.getInitParameter(ConfigProperty) != null) {
			logger.info("External configuration not specified, loading configuration from web.xml directly.");
			return reader.readValue(filterConfig.getInitParameter(ConfigProperty));
		} else {
			logger.error(
					"A Rate Limiting Filter is present but not configured, thus all rate limiting will be disabled.");
			return null;
		}
	}

	public static RateLimitingSettings fromString(String raw, String filterKey)
			throws JsonProcessingException, IOException {
		ObjectReader mapReader = mapper.reader(RateLimitingSettingsMap.class);
		RateLimitingSettingsMap map = mapReader.readValue(raw);
		if (map.getSettings().containsKey(filterKey))
			return map.getSettings().get(filterKey);
		return null;
	}

	private RateLimitingSettings loadConfigFromFile(String filename, String filterKey)
			throws JsonProcessingException, IOException {
		logger.info("Loading configuration from file " + filename);
		File file = new File(filename);
		if (!file.exists())
			throw new IllegalArgumentException("File " + filename + " doesn't exist!");

		String raw = FileUtils.readFileToString(file);
		return fromString(raw, filterKey);
	}

	private boolean isFileValid(String filename) {
		if (!StringUtils.isBlank(filename)) {
			logger.info("Checking file '" + filename + "'");
			if (new File(filename).exists())
				return true;
			else
				logger.warn("File " + filename + " does not exist");
		}
		return false;
	}

	/*
	 * first we will try to load file passed through system config property. if
	 * this file doesn't exist we will try to use file defined in web.xml
	 */
	private String findConfigFile(FilterConfig filterConfig) throws IOException, JsonProcessingException {

		String filename = System.getProperty(SystemConfigFileProperty);

		if (isFileValid(filename)) {
			logger.info("Using configuration file '" + filename + "' specified by system property '"
					+ SystemConfigFileProperty + "'");
			return filename;
		}

		filename = filterConfig.getInitParameter(SystemConfigFileProperty);
		if (isFileValid(filename)) {
			logger.info("Using configuration file '" + filename + "' specified in web.xml");
			return filename;
		}

		return null;
	}

	public RateLimitingSettings getSettings() {
		return result.settings;
	}

	public String getSettingsPath() {
		return result.settingsFile;
	}

	static class ConfigLoadResult {
		public RateLimitingSettings settings;
		public String settingsFile;
	}
}
