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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Before;
import org.junit.Test;

import bb.ratelimiting.filters.Filter;
import bb.ratelimiting.filters.PathFilter;
import bb.ratelimiting.filters.config.PathFilterSettings;

public class LabelHashTest {
	@Mocked
	HttpServletRequest mockRequest;

	private Label label;
	private List<Filter> filterList;

	@Before
	public void init() {
		PathFilterSettings settings1 = new PathFilterSettings();
		settings1.setExtract(1);
		settings1.setPath("/hello/(.+)/cats/aresoft/(.+)/times");

		PathFilterSettings settings2 = new PathFilterSettings();
		settings2.setExtract(2);
		settings2.setPath("/hello/(.+)/cats/aresoft/(.+)/times");

		filterList = new ArrayList<Filter>();
		filterList.add(new PathFilter("p1", settings1));
		filterList.add(new PathFilter("p2", settings2));

		label = new Label(filterList, null, "l1");
	}

	void resetFilters() {
		for (Filter f : filterList) {
			f.resetCache();
		}
	}

	@Test
	public void testCollisions() throws Exception {
		long collisionCount = 0, sampleSize = 10000;
		Random random = new Random();
		Set<Integer> uniqueInts = new HashSet<Integer>();

		for (int i = 0; i < sampleSize; i++) {
			final int num1 = random.nextInt() % 99999999;
			final int num2 = random.nextInt() % 99999999;

			new NonStrictExpectations() {
				{
					mockRequest.getRequestURI();
					result = "/hello/" + num1 + "/cats/aresoft/" + num2 + "/times";
				}
			};

			resetFilters();

			int hash = label.getLabelHash(mockRequest);
			if (uniqueInts.contains(hash)) {
				collisionCount++;
			}
			uniqueInts.add(hash);
		}

		double collisionPPM = ((double) collisionCount / (double) sampleSize) * (1000000.0);

		System.out.println("Hash collisions occur at a rate of " + collisionPPM + " PPM");

		if (collisionPPM > 1000)
			Assert.fail("Hash collision rate is greater than 1000 PPM");

	}

	@Test
	public void testPerformance() throws Exception {
		long sampleSize = 10000;

		new NonStrictExpectations() {
			{
				mockRequest.getRequestURI();
				result = "/hello/1000/cats/aresoft/5/times";
			}
		};

		long start = System.currentTimeMillis();
		for (int i = 0; i < sampleSize; i++) {
			resetFilters();
			label.getLabelHash(mockRequest);
		}
		long time = System.currentTimeMillis() - start;

		System.out.println("Performance of " + (sampleSize / (time / 1000000000.0)) + " requests per second");

	}
}
