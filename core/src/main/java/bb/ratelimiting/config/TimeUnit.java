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

/*
 * Time units in ms
 */
public enum TimeUnit {

	Millisecond(new String[] { "ms", "millisecond" }, 1), Second(new String[] { "s", "sec", "second" }, 1000), Minute(
			new String[] { "min", "minute" }, 1000 * 60), Hour(new String[] { "hr", "h", "hour" },
					1000 * 60 * 60), Day(new String[] { "day" }, 1000 * 60 * 60 * 24);

	public String[] names;
	public double millisecondsDuration;

	private TimeUnit(String[] names, double millisecondsDuration) {
		this.names = names;
		this.millisecondsDuration = millisecondsDuration;
	}

	public static TimeUnit fromString(String timeUnitDescriptor) {
		for (TimeUnit t : TimeUnit.values()) {
			for (String name : t.names) {
				if (name.equalsIgnoreCase(timeUnitDescriptor)) {
					return t;
				}
			}
		}
		throw new IllegalArgumentException("No TimeUnit matches " + timeUnitDescriptor);
	}

}
