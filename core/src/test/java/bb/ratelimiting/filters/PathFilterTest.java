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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import bb.ratelimiting.filters.config.PathFilterSettings;

public class PathFilterTest {

	@Mocked
	HttpServletRequest mockRequest;

	private List<String> httpMethods = Arrays.asList(new String[] { "PUT", "HEAD" });

	@Test
	public void staticMatch_HttpMethod_DoesntMatch() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setPath(".*");
		settings.setHttpMethods(httpMethods);

		new Expectations() {
			{

				mockRequest.getMethod();
				result = "GET";

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";
				maxTimes = 0;

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertNull(filter.filter(mockRequest));
	}

	@Test
	public void staticMatch_HttpMethod_Matches1() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setPath(".*");
		settings.setHttpMethods(httpMethods);

		new Expectations() {
			{

				mockRequest.getMethod();
				result = "HEAD";

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(".*", filter.filter(mockRequest));
	}

	@Test
	public void staticMatch_HttpMethod_Matches2() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setPath(".*");
		settings.setHttpMethods(null);

		new Expectations() {
			{

				mockRequest.getMethod();
				result = "GET";
				minTimes = 0;

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(".*", filter.filter(mockRequest));
	}

	@Test
	public void staticMatch() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setPath(".*");

		new Expectations() {
			{

				mockRequest.getMethod();
				result = "GET";
				minTimes = 0;

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(".*", filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch1() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(1);
		settings.setPath(".*/hello/(.+)/.*");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals("cats", filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch_ExtractWithNoGroupInRegex() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(1);
		settings.setPath("/hello/cats/aresoft");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(null, filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch_extractWithNoMatch() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(1);
		settings.setPath("/bye/dogs/arenotsoft");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(null, filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch_staticNoMatch() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setPath("CATSCATSCATSCATS");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/hello/cats/aresoft";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals(null, filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch_params() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(1);
		settings.setPath(".*?([0-9]{1})");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/?4";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);

		Assert.assertEquals("4", filter.filter(mockRequest));
	}

	@Test
	public void dynamicMatch_params_path() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(1);
		settings.setPath(".*/unit333/(.+)/service");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/unit333/DEADBEEF/service";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);
		int hash1 = filter.getFilterHash(mockRequest);

		Assert.assertEquals("DEADBEEF", filter.filter(mockRequest));

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/unit333/DEADBEEF22/service";

			}
		};

		filter.resetCache();
		int hash2 = filter.getFilterHash(mockRequest);

		Assert.assertEquals("DEADBEEF22", filter.filter(mockRequest));
		Assert.assertTrue("Hashes must be different: " + hash1 + "!=" + hash2, hash1 != hash2);
	}

	@Test
	public void staticMatch_params_path() throws Exception {
		PathFilterSettings settings = new PathFilterSettings();
		settings.setExtract(null);
		settings.setPath(".*/random/device/DEADBEEF");

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/random/device/DEADBEEF";

			}
		};

		PathFilter filter = new PathFilter("p1", settings);
		int hash1 = filter.getFilterHash(mockRequest);

		String result = filter.filter(mockRequest);
		Assert.assertEquals(settings.getPath(), result);

		new Expectations() {
			{

				mockRequest.getRequestURI();
				result = "http://localhost/random/device/DEADBEEF";

			}
		};

		filter.resetCache();
		int hash2 = filter.getFilterHash(mockRequest);
		result = filter.filter(mockRequest);

		Assert.assertEquals(settings.getPath(), result);
		Assert.assertTrue("Hashes must be the same: " + hash1 + "==" + hash2, hash1 == hash2);
	}
}
