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

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import bb.ratelimiting.filters.config.OriginFilterSettings;

public class OriginFilter extends Filter {
	private OriginFilterSettings settings;

	public OriginFilter(String filterName, OriginFilterSettings settings) {
		super(filterName, settings);
		this.settings = settings;
	}

	@SuppressWarnings("rawtypes")
	private String extractForwardFor(Enumeration forwardHeaders) {
		String forRegex = "[fF]or=(\\S+).*";
		Pattern p = Pattern.compile(forRegex);

		while (forwardHeaders.hasMoreElements()) {
			String header = (String) forwardHeaders.nextElement();

			Matcher m = p.matcher(header);
			if (!m.matches())
				continue;

			return m.group(1);
		}
		return null;
	}

	@Override
	public String filter(HttpServletRequest request) {
		if (!matchesHttpMethod(request)) {// don't apply filter unless HTTP
											// method matches defined in
											// settings
			return null;
		}

		String origin = null;

		origin = request.getHeader("X-Forwarded-For");

		if (origin == null) {
			origin = extractForwardFor(request.getHeaders("Forwarded"));
		}

		if (settings.isExpectProxied())
			return origin;
		else
			return request.getRemoteAddr();

	}

	@Override
	protected boolean isDynamic() {
		return true;
	}
}
