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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import junit.framework.Assert;
import mockit.Expectations;
import mockit.Mocked;

public class RateLimitingFilterTest {
	@Mocked
	FilterChain chain;

	@Mocked
	HttpServletRequest request;

	@Mocked
	HttpServletResponse response;

	@Mocked
	Label label;
	
	String header = "DISABLED_HEADER"; 
	Set<String> disabledForHeaders = new HashSet();
	{
		disabledForHeaders.add(header);
	}
	

	@Test
	public void testReplicatedRequest() throws ServletException, IOException {
		RateLimitingFilter filter = new RateLimitingFilter();
		filter.setEnabled(true);
		filter.setDisabledForHeaders(this.disabledForHeaders);

		new Expectations() {
			{
				request.getHeader(header);
				result = "NGC 2060";

				chain.doFilter(request, response);
				minTimes = 1;

				label.resetFilterCaches();
				maxTimes = 0;
			}
		};
		filter.doFilter(request, response, chain);
	}

	@Test
	public void test_RegularRequest_NoByPass() throws ServletException, IOException {
		RateLimitingFilter filter = new RateLimitingFilter();
		filter.setEnabled(true);
		filter.setDisabledForHeaders(disabledForHeaders);

		new Expectations() {
			{
				request.getHeader(header);
				result = null;

				chain.doFilter(request, response);
				minTimes = 0;

				label.resetFilterCaches();
				minTimes = 1;
			}
		};

		try {
			filter.doFilter(request, response, chain);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Filter has not been initialized properly", e.getMessage());
		}
	}


	@Test
	public void test_RegularRequest_UndefinedSetOfHeaders() throws ServletException, IOException {
		RateLimitingFilter filter = new RateLimitingFilter();
		filter.setEnabled(true);
		filter.labels = new ArrayList<Label>();

		new Expectations() {
			{
				chain.doFilter(request, response);
				minTimes = 1;

				label.resetFilterCaches();
				minTimes = 0;
			}
		};

		try {
			filter.doFilter(request, response, chain);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Filter has not been initialized properly", e.getMessage());
		}
	}

}
