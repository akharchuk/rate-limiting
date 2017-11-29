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

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class StatsManager {
	private Logger logger = Logger.getLogger(StatsManager.class);
	private Map<String, LabelStats> beanCache = new ConcurrentHashMap<String, LabelStats>();

	private JMXConfig jmxConfig;
	private String instanceId;

	public StatsManager(String instanceId, JMXConfig jmxConfig) {
		this.jmxConfig = jmxConfig;
		this.instanceId = instanceId;
	}

	private MBeanServer getMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	private ObjectName makeName(String labelName) throws MalformedObjectNameException {
		return new ObjectName(jmxConfig.getStatsDomainPrefix() + "." + instanceId + ":label=" + labelName);
	}

	public void unregisterBeans() {
		MBeanServer mbs = getMBeanServer();
		Iterator<Entry<String, LabelStats>> it = beanCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LabelStats> entry = it.next();
			ObjectName name;
			try {
				name = makeName(entry.getKey());
				logger.info("Removing bean " + name.toString());
				mbs.unregisterMBean(name);
				it.remove();
			} catch (MalformedObjectNameException e) {
				logger.warn("Error unregistering bean", e);
			} catch (MBeanRegistrationException e) {
				logger.warn("Error unregistering bean", e);
			} catch (InstanceNotFoundException e) {
				logger.warn("Error unregistering bean", e);
			}
		}
	}

	private LabelStats getBean(String key) {
		if (!beanCache.containsKey(key)) {
			synchronized (beanCache) {
				// Check again to see if it was added while waiting
				if (!beanCache.containsKey(key)) {
					LabelStats bean = new LabelStats();
					beanCache.put(key, bean);

					String logName = key;
					try {
						ObjectName name = makeName(key);
						logName = name.toString();
						MBeanServer mbs = getMBeanServer();
						Set<ObjectInstance> result = mbs.queryMBeans(name, null);
						if (!result.isEmpty()) {
							logger.error("Bean '" + name.toString() + "' is registered, but not present in cache."
									+ " Thus, the existing bean will be removed and replaced");
							mbs.unregisterMBean(name);
						}
						mbs.registerMBean(bean, name);
						logger.info("Registered LabelStatsMBean with name='" + logName + "'");

					} catch (InstanceAlreadyExistsException e) {
						logger.error("Error registering LabelStatsMBean with name='" + logName + "'", e);
					} catch (MBeanRegistrationException e) {
						logger.error("Error registering LabelStatsMBean with name='" + logName + "'", e);
					} catch (NotCompliantMBeanException e) {
						logger.error("Error registering LabelStatsMBean with name='" + logName + "'", e);
					} catch (MalformedObjectNameException e) {
						logger.error("Error creating name for object with key='" + key + "'", e);
					} catch (InstanceNotFoundException e) {
						logger.error("Error unregistering bean");
					}
				}
			}
		}

		return beanCache.get(key);
	}

	public void record(String key, boolean blocked) {
		LabelStats bean = getBean(key);
		if (bean == null) {
			logger.warn("Bean cannot be retrieved, no logging will be performed");
			return;
		}

		bean.totalCount++;
		if (blocked)
			bean.blockedCount++;
	}
}
