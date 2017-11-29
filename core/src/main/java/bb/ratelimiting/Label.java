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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import bb.ratelimiting.config.LabelDefinition;
import bb.ratelimiting.config.RateLimit;
import bb.ratelimiting.config.RateLimitingSettings;
import bb.ratelimiting.filters.Filter;
import bb.ratelimiting.filters.HeaderFilter;
import bb.ratelimiting.filters.OriginFilter;
import bb.ratelimiting.filters.PathFilter;
import bb.ratelimiting.filters.config.BaseFilterSettings;
import bb.ratelimiting.filters.config.HeaderFilterSettings;
import bb.ratelimiting.filters.config.OriginFilterSettings;
import bb.ratelimiting.filters.config.PathFilterSettings;
import bb.ratelimiting.util.HashUtil;

public class Label {
	private List<Filter> filters;
	private RateLimit rateLimit;
	private String name;

	public Label(List<Filter> filters, RateLimit rateLimit, String name) {
		this.filters = filters;
		this.rateLimit = rateLimit;
		this.name = name;
	}

	public int getLabelHash(HttpServletRequest request) {
		int result = 0;
		for (Filter filter : filters) {
			int newHash = filter.getFilterHash(request);
			if (newHash == 0)
				return 0;
			result = HashUtil.HashCombine(result, newHash);
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public RateLimit getRateLimit() {
		return rateLimit;
	}

	private static Map<String, Filter> filterMap = new HashMap<String, Filter>();

	private static Filter getFilter(String uid, String filterName, Map<String, BaseFilterSettings> filterSettingMap) {
		String localFilterId = uid + filterName;
		if (!filterMap.containsKey(localFilterId)) {
			if (!filterSettingMap.containsKey(filterName))
				throw new IllegalArgumentException("Filter '" + filterName + "' not found in configuration");

			Filter newFilter;
			BaseFilterSettings settings = filterSettingMap.get(filterName);

			if (settings instanceof PathFilterSettings)
				newFilter = new PathFilter(localFilterId, (PathFilterSettings) settings);
			else if (settings instanceof OriginFilterSettings)
				newFilter = new OriginFilter(localFilterId, (OriginFilterSettings) settings);
			else if (settings instanceof HeaderFilterSettings)
				newFilter = new HeaderFilter(localFilterId, (HeaderFilterSettings) settings);
			else
				throw new IllegalArgumentException("Invalid filter type: " + settings.getType());

			filterMap.put(localFilterId, newFilter);
		}

		return filterMap.get(localFilterId);
	}

	public static Label fromLabelDefinition(LabelDefinition ld, RateLimitingSettings rootConfig, String instanceKey) {
		List<Filter> filters = new ArrayList<Filter>();
		for (String filterName : ld.getFilters()) {
			filters.add(getFilter(instanceKey, filterName, rootConfig.getFilterDefinitions()));
		}

		RateLimit rateLimit = new RateLimit(ld.getRateLimit());

		return new Label(filters, rateLimit, ld.getName());
	}

	public static void clearFilters() {
		filterMap.clear();
	}

	public static void resetFilterCaches() {
		for (Filter f : filterMap.values()) {
			f.resetCache();
		}
	}

}