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

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import bb.ratelimiting.config.RateLimit;
import bb.ratelimiting.config.TimeUnit;

import com.hazelcast.core.IMap;

/*
 * An exponential moving average calculator
 */
public class EMARateCalculator implements RateCalculator {
	private Map<Integer, EMARateHistory> rateRecordMap;

	private Logger logger = Logger.getLogger(EMARateCalculator.class);

	private final static long MinLockWaitTime = 10L; 
	private final static java.util.concurrent.TimeUnit LOCK_TIME_UNIT = java.util.concurrent.TimeUnit.MILLISECONDS;
	private final double TWO = 2.0; 

	private final ReentrantLock lock = new ReentrantLock();

	public EMARateCalculator(Map<Integer, EMARateHistory> rateRecordMap) {
		if (rateRecordMap == null)
			throw new IllegalArgumentException("rateRecordMap cannot be null");

		this.rateRecordMap = rateRecordMap;
	}

	private double calculateRate(double timeConstant, double lastRate, long timePassed) {
		timePassed = Math.max(timePassed, 1);
		double alpha = 1.0 - Math.exp(-timePassed / timeConstant);
		return lastRate + alpha * ((1.0 / timePassed) - lastRate);
	}

	/*
	 * Locks either HZ map or local in-memory map Local lock is not efficient as
	 * it locks whole map. Can be refactored later.
	 */
	private boolean lock(int hash, long waitTime) {

		if (rateRecordMap instanceof IMap) {
			if (logger.isDebugEnabled())
				logger.debug("Getting distributed lock for hash " + hash);
			return ((IMap) rateRecordMap).tryLock(hash, waitTime, LOCK_TIME_UNIT);
		} else {
			try {
				if (logger.isDebugEnabled())
					logger.debug("Getting local lock for hash " + hash);
				return lock.tryLock(waitTime, LOCK_TIME_UNIT);
			} catch (InterruptedException e) {
				logger.error("Failed to acquire lock for hash " + hash + ": " + e.getMessage());
				return false;
			}
		}
	}

	private void storeAndUnlock(int hash, EMARateHistory history) {

		if (rateRecordMap instanceof IMap) {
			((IMap) rateRecordMap).putAndUnlock(hash, history);
		} else {
			rateRecordMap.put(hash, history);
			lock.unlock();
		}

	}

	@Override
	public boolean exceedsRate(int hash, RateLimit limit) {
		boolean exceeded = false;
		double normalizedRate = limit.getNormalizedRate(TimeUnit.Millisecond);

		long waitTime = Math.max((long) (TWO / normalizedRate), MinLockWaitTime);

		if (lock(hash, waitTime)) {
			// **** Synchronized Block Start ********
			EMARateHistory history = null;
			try {
				history = rateRecordMap.get(hash);

				if (history == null)
					history = new EMARateHistory();

				long now = System.currentTimeMillis();
				long timePassed = (now - history.getLastTime());
				double timeConstant = limit.getTimeConstant();

				double newRate = calculateRate(timeConstant, history.getLastRate(), timePassed);

				if (logger.isDebugEnabled())
					logger.debug("timePassed=" + timePassed + ", newRate=" + newRate + ", normalizedRate="
							+ normalizedRate + " for hash: " + hash);

				history.setLastRate(newRate);
				history.setLastTime(now);
				exceeded = newRate > normalizedRate;
			} finally {
				storeAndUnlock(hash, history);
			}
			// ****** End Synchronized Block **********

			if (logger.isDebugEnabled())
				logger.debug("exceedsRate=" + exceeded + " for hash: " + hash + ", limit=" + limit);

			return exceeded;
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Failed to acquire lock for hash: " + hash + ", limit=" + limit);
		}

		// We couldn't lock this hash in time, which means it's being heavily
		// accessed by other nodes in the clusted,
		// and thus almost certainly exceeding its specified rate
		return true;
	}

}
