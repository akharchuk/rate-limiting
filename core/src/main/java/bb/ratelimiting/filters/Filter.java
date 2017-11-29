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

import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import bb.ratelimiting.filters.config.BaseFilterSettings;
import bb.ratelimiting.util.HashUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.hash.Hashing;

@JsonInclude(Include.NON_EMPTY)
public abstract class Filter {
	private int filterSalt;

	private int cachedResult;
	private boolean cacheInvalid;

	protected List<String> httpMethods;

	public List<String> getHttpMethods() {
		return httpMethods;
	}

	protected boolean matchesHttpMethod(HttpServletRequest request) {

		if (httpMethods == null || httpMethods.size() == 0)// if this part of
															// configuration is
															// missing we allow
															// all HTTP methods
			return true;

		String method = request.getMethod();

		for (String m : httpMethods) {
			if (m.equalsIgnoreCase(method))
				return true;
		}

		return false;
	}

	public Filter(String filterUniqueIdentifier, BaseFilterSettings settings) {
		this.filterSalt = Hashing.murmur3_32().hashString(filterUniqueIdentifier, Charset.forName("UTF-8")).asInt();
		cacheInvalid = true;
		this.httpMethods = settings.getHttpMethods();
	}

	public int getFilterHash(HttpServletRequest request) {
		// don't use cache for dynamic filters (e.g. path filter)
		if (cacheInvalid || isDynamic()) {
			String result = filter(request);
			if (result == null)
				cachedResult = 0;
			else {
				cachedResult = Hashing.murmur3_32().hashString(result, Charset.forName("UTF-8")).asInt();
				cachedResult = HashUtil.HashCombine(cachedResult, filterSalt);
			}
			cacheInvalid = false;
		}
		return cachedResult;
	}

	public void resetCache() {
		cacheInvalid = true;
	}

	protected abstract String filter(HttpServletRequest request);

	protected abstract boolean isDynamic();
}
