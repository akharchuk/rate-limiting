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
package bb.ratelimiting.util;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HzUtil {

	public static String HZ_CONFIG_FILE = "hazelcast.xml";

	private static Logger logger = Logger.getLogger(HzUtil.class);

	static String CALLER = HzUtil.class.getName();

	public static HazelcastInstance loadHzConfiguration(String configFilePath) {
		HazelcastInstance h1 = null;
		try {
			Config config = null;
			if (!StringUtils.isBlank(configFilePath)) {
				logger.info("Loading Hazelcast config from path: " + configFilePath);
				String xml = IOUtils.toString(new FileReader(new File(configFilePath)));
				xml = StringUtil.replace(xml, System.getProperties());
				config = new InMemoryXmlConfig(xml);

			} else {
				config = new ClasspathXmlConfig(HZ_CONFIG_FILE);
			}

			logger.info("Loaded Hazelcast config: " + config.toString());

			h1 = Hazelcast.newHazelcastInstance(config);
		} catch (Exception e) {
			logger.warn("NO HAZELCAST CONFIGURATION FOUND: " + e.getMessage(), e);
			h1 = Hazelcast.newHazelcastInstance();
			logger.info("Using default config");
		}
		return h1;
	}

}
