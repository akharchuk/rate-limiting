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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import bb.ratelimiting.config.RateLimitingSettings;
import bb.ratelimiting.filters.config.RateLimitingSettingsMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;

public class DistributedConfigManager {
	private final static String JmxName = "RateLimitingConfiguration";

	private static Logger logger = Logger.getLogger(DistributedConfigManager.class);
	private IMap<String, RateLimitingSettings> configMap;
	private ConfigurationAccessor accessor;
	private ConfigWatcher<RateLimitingSettings> externalWatcher;
	private String configKey;
	private String configPersistencePath;

	private static ObjectMapper mapper = new ObjectMapper();

	public DistributedConfigManager(String key, RateLimitingSettings initial, IMap<String, RateLimitingSettings> map,
			ConfigWatcher<RateLimitingSettings> watcher, String configPersistencePath) {
		this.configMap = map;
		this.configKey = key;
		this.externalWatcher = watcher;
		this.configPersistencePath = configPersistencePath;

		JMXConfig jmxConf = initial.getJmxConfig();
		if (jmxConf != null) {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			accessor = new ConfigurationAccessor(new ConfigWatcher<RateLimitingSettings>() {
				@Override
				public void configChanged(RateLimitingSettings settings) {
					externalWatcher.configChanged(settings);

					logger.info("Configuration changed with MBean, persisting to map");
					try {
						configMap.put(configKey, settings);
					} catch (Exception e) {
						logger.error("Error putting", e);
					}

					persistConfigurationMap();
				}

				@Override
				public RateLimitingSettings refreshConfig() {
					RateLimitingSettings newSettings = externalWatcher.refreshConfig();
					logger.info("Configuration refreshed, persisting to map");
					try {
						configMap.put(configKey, newSettings);
					} catch (Exception e) {
						logger.error("Error putting", e);
					}
					return newSettings;
				}
			}, initial);

			try {
				ObjectName name = new ObjectName(jmxConf.getConfigDomainPrefix() + "." + key + ":name=" + JmxName);
				logger.info("Registering configuration bean with name=" + name.toString());
				mbs.registerMBean(accessor, name);
			} catch (Exception e) {
				logger.error("Error registering configuration MBean with key='" + key
						+ "'. Will proceed without dynamic configuration.", e);
			}
		} else {
			logger.warn("No JMX settings present, configuration will not be externally managed");
		}

		configMap.put(key, initial);
		map.addEntryListener(new EntryWatcher(new ConfigWatcher<RateLimitingSettings>() {
			@Override
			public void configChanged(RateLimitingSettings settings) {
				logger.info("Configuration updated in map; propagating");
				accessor.updateConfiguration(settings);
				externalWatcher.configChanged(settings);
				persistConfigurationMap();
			}

			@Override
			public RateLimitingSettings refreshConfig() {
				return externalWatcher.refreshConfig();
			}
		}), key, true);
	}

	synchronized void persistConfigurationMap() {
		if (configPersistencePath != null) {
			logger.info("Persisting new configuration to path '" + configPersistencePath + "'");

			ObjectReader reader = mapper.reader(RateLimitingSettingsMap.class);

			RateLimitingSettingsMap configPersistMap = null;

			try {
				configPersistMap = reader.readValue(new File(configPersistencePath));
			} catch (JsonProcessingException e1) {
				logger.error(e1);
			} catch (IOException e1) {
				logger.error(e1);
			}

			if (configPersistMap == null) {
				configPersistMap = new RateLimitingSettingsMap();
				configPersistMap.setSettings(new HashMap<String, RateLimitingSettings>(configMap));
			} else {
				configPersistMap.getSettings().putAll(configMap);
			}

			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
			try {
				writer.writeValue(new File(configPersistencePath), configPersistMap);
			} catch (JsonGenerationException e) {
				logger.error("Error persisting configuration", e);
			} catch (JsonMappingException e) {
				logger.error("Error persisting configuration", e);
			} catch (IOException e) {
				logger.error("Error persisting configuration", e);
			}
		}
	}

	static class EntryWatcher implements EntryListener<String, RateLimitingSettings> {
		ConfigWatcher<RateLimitingSettings> watcher;

		public EntryWatcher(ConfigWatcher<RateLimitingSettings> watcher) {
			this.watcher = watcher;
		}

		@Override
		public void entryAdded(EntryEvent<String, RateLimitingSettings> paramEntryEvent) {
			if (paramEntryEvent.getMember().localMember()) {
				logger.debug("Configuration added by local member, ignoring");
				return;
			}
			watcher.configChanged(paramEntryEvent.getValue());
		}

		@Override
		public void entryRemoved(EntryEvent<String, RateLimitingSettings> paramEntryEvent) {

		}

		@Override
		public void entryUpdated(EntryEvent<String, RateLimitingSettings> paramEntryEvent) {
			if (paramEntryEvent.getMember().localMember()) {
				logger.debug("Configuration change affected by local member, ignoring");
				return;
			}
			watcher.configChanged(paramEntryEvent.getValue());
		}

		@Override
		public void entryEvicted(EntryEvent<String, RateLimitingSettings> paramEntryEvent) {
			logger.error("Configuration entry was evicted. All configuration should be stored in a non-expiring map!!");
		}
	}

}
