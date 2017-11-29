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

import org.apache.log4j.Logger;

import bb.ratelimiting.filters.config.HeaderFilterSettings;

public class HeaderFilter extends Filter {
	private HeaderFilterSettings settings;
	private Pattern headerPattern;
	Logger logger = Logger.getLogger(HeaderFilter.class);

	public HeaderFilter(String filterName, HeaderFilterSettings settings) {
		super(filterName, settings);
		this.settings = settings;

		if (settings.getExpression() != null)
			headerPattern = Pattern.compile(settings.getExpression());
	}

	@Override
	public String filter(HttpServletRequest request) {
		if (!matchesHttpMethod(request)) {// don't apply filter unless HTTP
											// method matches defined in
											// settings
			return null;
		}

		@SuppressWarnings("rawtypes")
		Enumeration headers = request.getHeaders(settings.getHeaderName());

		while (headers.hasMoreElements()) {
			String header = (String) headers.nextElement();

			if (headerPattern != null) {
				Matcher m = headerPattern.matcher(header);
				if (!m.matches())
					continue;

				if (settings.getExtractIndex() > 0)
					return m.group(settings.getExtractIndex());
				else
					return m.group(0);
			}
			return header;
		}
		return null;
	}

	@Override
	protected boolean isDynamic() {
		return true;
	}
}
