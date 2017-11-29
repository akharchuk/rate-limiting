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
package bb.ratelimiting.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import bb.ratelimiting.filters.config.PathFilterSettings;

public class PathFilter extends Filter {
	private static Logger logger = Logger.getLogger(PathFilter.class);

	private bb.ratelimiting.filters.config.PathFilterSettings settings;
	private Pattern pathPattern;

	public PathFilter(String filterName, PathFilterSettings settings) {
		super(filterName, settings);
		this.settings = settings;
		pathPattern = Pattern.compile(settings.getPath());

		logger.info("Created path filter with extract index = " + settings.getExtractIndex());
	}

	@Override
	public String filter(HttpServletRequest request) {
		if (!matchesHttpMethod(request)) {// don't apply filter unless HTTP
											// method matches defined in
											// settings
			return null;
		}

		String path = request.getRequestURI()
				+ (request.getQueryString() != null ? "?" + request.getQueryString() : "");
		Matcher matcher = pathPattern.matcher(path);
		String result = null;

		if (matcher.matches()) {
			if (settings.getExtractIndex() != null) {
				if (matcher.groupCount() > 0) {
					result = matcher.group(settings.getExtractIndex());
				}
			} else {
				result = settings.getPath();
			}
		}

		return result;
	}

	public boolean isDynamic() {
		boolean isDynamic = settings.getExtractIndex() != null;
		return isDynamic;
	}

	@Override
	public String toString() {
		return "PathFilter [settings=" + settings + ", pathPattern=" + pathPattern + "]";
	}

}
