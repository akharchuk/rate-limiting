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
package bb.ratelimiting.config;

import junit.framework.Assert;

import org.junit.Test;

public class RateLimitConversionTest {
	public RateLimit makeConfig(String rateDescriptor) {
		RawRateLimit raw = new RawRateLimit();
		raw.setRateDescriptor(rateDescriptor);
		raw.setLimitExceedResponse("error");
		return new RateLimit(raw);
	}

	@Test
	public void descriptor1() {
		Assert.assertEquals(10.0, makeConfig("10/day").getNormalizedRate(TimeUnit.Day));
	}

	@Test
	public void descriptor2() {
		Assert.assertEquals(20.0, makeConfig("20/second").getNormalizedRate(TimeUnit.Second));
	}

	@Test
	public void unitConversions() {

		RateLimit r = makeConfig("100/s");
		Assert.assertEquals(0.1, r.getNormalizedRate(TimeUnit.Millisecond));
		Assert.assertEquals(100.0, r.getNormalizedRate(TimeUnit.Second));

		r = makeConfig("1/ms");
		Assert.assertEquals(1.0, r.getNormalizedRate(TimeUnit.Millisecond));
		Assert.assertEquals(1000.0, r.getNormalizedRate(TimeUnit.Second));
		Assert.assertEquals(60000.0, r.getNormalizedRate(TimeUnit.Minute));
		Assert.assertEquals(3600000.0, r.getNormalizedRate(TimeUnit.Hour));
		Assert.assertEquals(86400000.0, r.getNormalizedRate(TimeUnit.Day));

		r = makeConfig("120000/hour");
		Assert.assertEquals(0.0333, r.getNormalizedRate(TimeUnit.Millisecond), 0.001);
		Assert.assertEquals(33.3333, r.getNormalizedRate(TimeUnit.Second), 0.0001);
		Assert.assertEquals(2000.0, r.getNormalizedRate(TimeUnit.Minute));
		Assert.assertEquals(120000.0, r.getNormalizedRate(TimeUnit.Hour));
		Assert.assertEquals(2880000.0, r.getNormalizedRate(TimeUnit.Day));

	}
}
