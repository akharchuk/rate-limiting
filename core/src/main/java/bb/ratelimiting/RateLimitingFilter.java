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
package bb.ratelimiting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import static org.apache.commons.lang3.StringUtils.*;
import org.apache.log4j.Logger;

import bb.ratelimiting.calc.EMARateCalculator;
import bb.ratelimiting.calc.EMARateHistory;
import bb.ratelimiting.calc.RateCalculator;
import bb.ratelimiting.config.ConfigurationLoader;
import bb.ratelimiting.config.LabelDefinition;
import bb.ratelimiting.config.RateExceededResponse;
import bb.ratelimiting.config.RateLimitingSettings;
import bb.ratelimiting.jmx.ConfigWatcher;
import bb.ratelimiting.jmx.DistributedConfigManager;
import bb.ratelimiting.jmx.StatsManager;
import bb.ratelimiting.util.HzUtil;
import bb.ratelimiting.util.MultiReadHttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class RateLimitingFilter implements javax.servlet.Filter, ConfigWatcher<RateLimitingSettings> {
	
	private static Logger logger = Logger.getLogger(RateLimitingFilter.class);
	private static final String HZ_FILE = "HZ_FILE";

	protected String filterId;
	protected Set<String> disabledForHeaders;
	protected List<Label> labels;
	protected RateCalculator rateCalculator;
	protected boolean enabled;
	protected static HazelcastInstance hazelcastInstance;
	protected DistributedConfigManager configManager;
	protected StatsManager statsManager;
	protected ConfigurationLoader loader;


	private String getHazelcastConfig(RateLimitingSettings initSettings) {

		String hzConfig = initSettings.getHazelcastConfig().getHzConfigFile();
		if (isBlank(hzConfig)) {
			hzConfig = System.getProperty(HZ_FILE);
		}

		return hzConfig;
	}

	/*
	 * Starts new HZ instance if hasn't been initialized
	 */
	protected void initHazelcast(RateLimitingSettings initSettings) {
		if (getHZ() == null) {
			String hzConfig = getHazelcastConfig(initSettings);
			if (isBlank(hzConfig)) {
				logger.warn("Starting new local instance of Hazelcast, valid only during testing!!!");
				logger.warn("You can pass the location of HZ configuration using 'HZ_FILE' system property");
				hazelcastInstance = Hazelcast.newHazelcastInstance();
			} else {
				hazelcastInstance = HzUtil.loadHzConfiguration(hzConfig);
			}
		}
	}

	protected void setHZ(HazelcastInstance instance) {
		hazelcastInstance = instance;
	}

	protected HazelcastInstance getHZ() {
		return hazelcastInstance;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		initializeIdentifier(filterConfig);
		logger.info("Initializing RateLimitingFilter with ID=" + filterId);

		try {
			loader = new ConfigurationLoader(filterId);
			loader.loadConfig(filterConfig);

			RateLimitingSettings initSettings = loader.getSettings();

			if (initSettings != null) {
				initHazelcast(initSettings);
				applyConfig(initSettings);

				String mapName = initSettings.getHazelcastConfig().getConfigMapName();
				IMap<String, RateLimitingSettings> configMap = getHZ().getMap(mapName);
				if (configMap == null) {
					throw new IllegalArgumentException("Failed to load HZ map: " + mapName);
				}

				configManager = new DistributedConfigManager(filterId, initSettings, configMap, this,
						(initSettings.isPersistConfig() ? loader.getSettingsPath() : null));

				if (initSettings.getJmxConfig() != null) {
					statsManager = new StatsManager(filterId, initSettings.getJmxConfig());
				} else {
					logger.warn("No JMX configuration is present, statistics will not be reported");
				}
			}

		} catch (JsonProcessingException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (IllegalArgumentException iae) {
			throw new ServletException(iae);
		}
	}

	/**
	 * The filterId uniquely identifies this rate limiting filter among others.
	 * Once set it will not be changed.
	 */
	private void initializeIdentifier(FilterConfig filterConfig) {
		if (filterId == null) {
			filterId = filterConfig.getFilterName();
		}
	}

	private void applyConfig(RateLimitingSettings newSettings) {
		logger.info("applying new rate limiting settings");
		enabled = newSettings.isEnabled();
		if (!enabled) {
			logger.warn(
					"Rate limiting is configured to be disabled! Set 'isEnabled' to 'true' in the configuration file to activate");
			return;
		}

		if (statsManager != null)
			statsManager.unregisterBeans();

		Label.clearFilters();
		disabledForHeaders = newSettings.getDisabledForHeaders();

		labels = new ArrayList<Label>();

		if (newSettings.getLabels() != null)
			for (LabelDefinition ld : newSettings.getLabels()) {
				if (ld.isEnabled())
					labels.add(Label.fromLabelDefinition(ld, newSettings, filterId));
			}

		logger.info("Loaded " + labels.size() + " label" + (labels.size() == 1 ? "" : "s"));

		IMap<Integer, EMARateHistory> map = getHZ().getMap(newSettings.getHazelcastConfig().getMapName());
		rateCalculator = new EMARateCalculator(map);
	}

	@Override
	public void configChanged(RateLimitingSettings newSettings) {
		logger.info("Applying new settings");
		applyConfig(newSettings);
	}

	@Override
	public RateLimitingSettings refreshConfig() {
		logger.info("Refreshing configuration");
		try {
			loader.reloadConfig();
			applyConfig(loader.getSettings());
			return loader.getSettings();
		} catch (IOException e) {
			logger.error("Error reloading configuration", e);
		}
		return null;
	}

	@Override
	public void destroy() {
		if (getHZ() != null && getHZ().getLifecycleService() != null) {
			getHZ().getLifecycleService().shutdown();
		}

		if (statsManager != null)
			statsManager.unregisterBeans();
	}

	private RateExceededResponse rateExceeded(HttpServletRequest request) {
		Label.resetFilterCaches();

		if (labels == null)
			throw new IllegalArgumentException("Filter has not been initialized properly");

		RateExceededResponse response = null;
		for (Label label : labels) {
			int hash = label.getLabelHash(request);

			boolean labelExceeds = false;
			if (hash != 0)
				labelExceeds = rateCalculator.exceedsRate(hash, label.getRateLimit());

			if (labelExceeds)
				response = label.getRateLimit().getRateExceededResponse();

			if (statsManager != null)
				statsManager.record(label.getName(), labelExceeds);
		}
		return response;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;

		// bypass rate limiting for replicated requests
		if (!enabled || alwaysAllow(httpReq)) {
			chain.doFilter(request, response);
			return;
		}

		RateExceededResponse rateResponse = rateExceeded(httpReq);

		if (rateResponse == null) {
			chain.doFilter(request, response);
		} else {
			logger.warn("Rate exceeded " + httpReq.getRequestURI());
			MultiReadHttpServletResponse multiReadServletResponse = new MultiReadHttpServletResponse(httpRes);
			switch (rateResponse) {
			case ReturnError:
				multiReadServletResponse.setStatus(429);
				httpRes.setContentType(MediaType.TEXT_PLAIN);
				httpRes.setContentLength(0);
				break;
			case Blackhole:
				break;
			case FakeSuccess:
				multiReadServletResponse.setStatus(204);
				httpRes.setContentType(MediaType.TEXT_PLAIN);
				httpRes.setContentLength(0);
				break;
			}
		}
	}

	/*
	 * Used to determine if the rate limiting should be disabled (needed for rate limit override)
	 */
	private boolean alwaysAllow(HttpServletRequest request) {
		boolean result = false;
		
		if(disabledForHeaders!=null){
			for(String name : disabledForHeaders){
				if(isNotBlank(request.getHeader(name))){
					result = true;
					logger.debug("Bypassing rate limiting for request due to header: " + name);
					break;
				}
			}
		}

		return result;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setDisabledForHeaders(Set<String> disabledForHeaders) {
		this.disabledForHeaders = disabledForHeaders;
	}
	
}
