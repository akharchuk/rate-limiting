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

import bb.ratelimiting.filters.config.HeaderFilterSettings;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HeaderFilterTest {

	@Mocked
	HttpServletRequest mockRequest;

	private List<String> httpMethods = Arrays.asList(new String[] { "PUT", "HEAD" });

	@Test
	public void headerNoExpression_HttpMethodDoesntMatch() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");
		settings.setHttpMethods(httpMethods);

		HashSet<String> set = new HashSet<String>();
		set.add("hello");
		set.add("bye");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{
				mockRequest.getMethod();
				result = "GET";

				mockRequest.getHeaders("Cat");
				result = r;
				maxTimes = 0;
			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertNull(filter.filter(mockRequest));
	}

	@Test
	public void headerNoExpression() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");

		HashSet<String> set = new HashSet<String>();
		set.add("hello");
		set.add("bye");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Cat");
				result = r;

			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertEquals("hello", filter.filter(mockRequest));
	}

	@Test
	public void headerExpression1() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");
		settings.setExpression("hello([0-9])");

		HashSet<String> set = new HashSet<String>();
		set.add("hello");
		set.add("hello5");
		set.add("bye");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Cat");
				result = r;

			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertEquals("hello5", filter.filter(mockRequest));
	}

	@Test
	public void headerExpression2() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");
		settings.setExpression("hello([0-9])");
		settings.setExtractIndex(1);

		HashSet<String> set = new HashSet<String>();
		set.add("helloMeow");
		set.add("hello5");
		set.add("bye");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Cat");
				result = r;

			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertEquals("5", filter.filter(mockRequest));
	}

	@Test
	public void headerExpression3() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");
		settings.setExpression("hello|HELLO");

		HashSet<String> set = new HashSet<String>();
		set.add("Hello");
		set.add("HELLO");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Cat");
				result = r;

			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertEquals("HELLO", filter.filter(mockRequest));
	}

	@Test
	public void headerExpressionNull() throws Exception {
		HeaderFilterSettings settings = new HeaderFilterSettings();
		settings.setHeaderName("Cat");
		settings.setExpression("bye");

		HashSet<String> set = new HashSet<String>();
		set.add("Hello");
		set.add("HELLO");

		final Enumeration<String> r = new Vector(set).elements();

		new Expectations() {
			{

				mockRequest.getHeaders("Cat");
				result = r;

			}
		};

		Filter filter = new HeaderFilter("f1", settings);

		Assert.assertEquals(null, filter.filter(mockRequest));
	}

}
