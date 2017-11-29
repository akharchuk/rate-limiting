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
package bb.ratelimiting.calc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import bb.ratelimiting.config.RateExceededResponse;
import bb.ratelimiting.config.RateLimit;
import bb.ratelimiting.config.TimeUnit;
import junit.framework.Assert;

public class EMARateCalculatorTest {

	private Logger logger = Logger.getLogger(EMARateCalculatorTest.class);
	Map<Integer, EMARateHistory> rateRecordMap;

	TimeUnit timeUnit = TimeUnit.Second;
	RateExceededResponse rateExceededResponse = RateExceededResponse.ReturnError;

	@Before
	public void before() {
		this.rateRecordMap = new HashMap<Integer, EMARateHistory>();
		;
	}

	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {

		}
	}

	@Test
	public void testPathRate_DoesntExceed() throws IOException {

		double requestsPerTimeUnit = 1000;
		Double timeConstant = 0.3;

		// settings = ConfigTest.loadTestSettings();
		final int hash = 487587236;
		EMARateCalculator calculator = new EMARateCalculator(rateRecordMap);

		RateLimit limit = new RateLimit(timeUnit, requestsPerTimeUnit, rateExceededResponse, timeConstant);

		boolean exceeded = false;
		for (int i = 0; i < 99; i++) {
			exceeded = exceeded || calculator.exceedsRate(hash, limit);
		}
		Assert.assertFalse("We are not supposed to exceed rate", exceeded);

	}

	@Test
	public void testPathRate_Exceed() throws IOException {

		double requestsPerTimeUnit = 100;
		Double timeConstant = 0.3;

		// settings = ConfigTest.loadTestSettings();
		final int hash = 487587236;
		EMARateCalculator calculator = new EMARateCalculator(rateRecordMap);

		RateLimit limit = new RateLimit(timeUnit, requestsPerTimeUnit, rateExceededResponse, timeConstant);

		boolean exceeded = false;
		for (int i = 0; i < 200; i++) {
			exceeded = exceeded || calculator.exceedsRate(hash, limit);

			if (i < 1)
				Assert.assertFalse("We are not supposed to exceed rate", exceeded);
		}
		Assert.assertTrue("We were supposed to exceed rate", exceeded);
	}

	@Test
	public void testPathRate_Exceed_And_Recover() throws IOException {

		double requestsPerTimeUnit = 100;
		Double timeConstant = 0.3;

		// settings = ConfigTest.loadTestSettings();
		final int hash = 487587236;
		EMARateCalculator calculator = new EMARateCalculator(rateRecordMap);

		RateLimit limit = new RateLimit(timeUnit, requestsPerTimeUnit, rateExceededResponse, timeConstant);

		boolean exceeded = false;
		for (int i = 0; i < 200; i++) {
			exceeded = exceeded || calculator.exceedsRate(hash, limit);

			if (i < 1)
				Assert.assertFalse("We are not supposed to exceed rate", exceeded);
		}
		Assert.assertTrue("We were supposed to exceed rate at least once", exceeded);
		sleep(1000);

		exceeded = false;
		for (int i = 0; i < 10; i++) {
			exceeded = calculator.exceedsRate(hash, limit);
			sleep(200);

		}
		Assert.assertFalse("We are not supposed to exceed rate eventually", exceeded);
	}

	@Test
	@Ignore
	public void testPathRate_Exceed_And_Recover2() throws IOException {

		double requestsPerTimeUnit = 10;
		Double timeConstant = 0.3;

		// settings = ConfigTest.loadTestSettings();
		final int hash = 487587236;
		EMARateCalculator calculator = new EMARateCalculator(rateRecordMap);

		RateLimit limit = new RateLimit(timeUnit, requestsPerTimeUnit, rateExceededResponse, timeConstant);

		boolean exceeded = false;
		int dropped = 0;
		for (int i = 0; i < 2000; i++) {
			exceeded = calculator.exceedsRate(hash, limit) || exceeded;
			if (exceeded)
				dropped++;

			if (i < 1)
				Assert.assertFalse("We are not supposed to exceed rate", exceeded);
		}
		logger.info(">>>>>>>>>>>>>>>>>Dropped " + dropped + " requests");

		Assert.assertTrue("We were supposed to exceed rate at least once", exceeded);
		sleep(20);

		dropped = 0;
		exceeded = false;
		for (int i = 0; i < 10; i++) {
			exceeded = calculator.exceedsRate(hash, limit);
			if (exceeded)
				dropped++;
			sleep(50);
		}

		logger.info(">>>>>>>>>>>>>>>>>>Dropped " + dropped + " requests");

		Assert.assertTrue("We are not supposed to exceed rate eventually", dropped < 10);
	}
}
