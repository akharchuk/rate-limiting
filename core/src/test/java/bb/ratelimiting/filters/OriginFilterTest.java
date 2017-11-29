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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import bb.ratelimiting.filters.config.OriginFilterSettings;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OriginFilterTest {

	@Mocked
	HttpServletRequest mockRequest;

	private List<String> httpMethods = Arrays.asList(new String[] { "PUT", "HEAD" });

	@Test
	public void forwardHeaderForOnly_HttpMethodDoesntMatch() throws Exception {
		OriginFilterSettings settings = new OriginFilterSettings();
		settings.setExpectProxied(true);
		settings.setHttpMethods(httpMethods);

		new Expectations() {
			{
				mockRequest.getMethod();
				result = "GET";

				mockRequest.getHeader("X-Forwarded-For");
				result = "192.168.2.1";
				maxTimes = 0;

			}
		};

		OriginFilter filter = new OriginFilter("f1", settings);

		Assert.assertNull(filter.filter(mockRequest));
	}

	@Test
	public void forwardHeaderForOnly() throws Exception {
		OriginFilterSettings settings = new OriginFilterSettings();
		settings.setExpectProxied(true);

		new Expectations() {
			{

				mockRequest.getHeader("X-Forwarded-For");
				result = "192.168.2.1";

			}
		};

		OriginFilter filter = new OriginFilter("f1", settings);

		Assert.assertEquals("192.168.2.1", filter.filter(mockRequest));
	}

	@Test
	public void forwardHeadersOnly() throws Exception {
		OriginFilterSettings settings = new OriginFilterSettings();
		settings.setExpectProxied(true);

		HashSet<String> set = new HashSet<String>();
		set.add("192.168.2.1");
		set.add("for=cats");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Forwarded");
				result = r;

			}
		};

		OriginFilter filter = new OriginFilter("f1", settings);

		Assert.assertEquals("cats", filter.filter(mockRequest));
	}

	@Test
	public void forwardHeadersOnlyCats() throws Exception {
		OriginFilterSettings settings = new OriginFilterSettings();
		settings.setExpectProxied(true);

		HashSet<String> set = new HashSet<String>();
		set.add("192.168.2.1");
		set.add("For=cats, soft=true");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Forwarded");
				result = r;

			}
		};

		OriginFilter filter = new OriginFilter("f1", settings);

		Assert.assertEquals("cats,", filter.filter(mockRequest));
	}

	@Test
	public void forwardHeadersOnlyQuotes() throws Exception {
		OriginFilterSettings settings = new OriginFilterSettings();
		settings.setExpectProxied(true);

		HashSet<String> set = new HashSet<String>();
		set.add("192.168.2.1");
		set.add("For=\"cats\", by=meow");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Forwarded");
				result = r;

			}
		};

		OriginFilter filter = new OriginFilter("f1", settings);

		Assert.assertEquals("\"cats\",", filter.filter(mockRequest));
	}

}
