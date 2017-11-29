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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RateLimit {
	private TimeUnit timeUnit;
	private RateExceededResponse rateExceededResponse;
	private double requestsPerTimeUnit;
	private Double timeConstant;

	public RateLimit(TimeUnit timeUnit, double requestsPerTimeUnit, RateExceededResponse rateExceededResponse,
			Double timeConstant) {
		this.timeUnit = timeUnit;
		this.requestsPerTimeUnit = requestsPerTimeUnit;
		this.timeConstant = timeConstant;
		this.rateExceededResponse = rateExceededResponse;
	}

	public RateLimit(RawRateLimit rawConfig) {

		String rd = StringUtils.trim(rawConfig.getRateDescriptor().toLowerCase());

		String[] descriptorComponents = null;

		if (rd.contains(" per "))
			descriptorComponents = rd.split(" per ");
		else if (rd.contains("/"))
			descriptorComponents = rd.split("/");

		if (descriptorComponents == null || descriptorComponents.length < 2)
			throw new IllegalArgumentException("Invalid rate descriptor string '" + rawConfig.getRateDescriptor()
					+ "'. Must contain be in format <x> per <time_unit> or <x>/<timeunit>");

		String requestsPerTimeUnitStr = StringUtils.trim(descriptorComponents[0]);
		String timeUnitStr = StringUtils.trim(descriptorComponents[1]);
		String rateExceededStr = StringUtils.trim(rawConfig.getRateExceededResponseDescriptor());

		try {
			requestsPerTimeUnit = Double.valueOf(requestsPerTimeUnitStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid rate descriptor string '" + rawConfig.getRateDescriptor()
					+ "'. Invalid numeric quantity '" + requestsPerTimeUnitStr + "'");
		}

		timeUnit = TimeUnit.fromString(timeUnitStr);

		rateExceededResponse = RateExceededResponse.fromString(rateExceededStr);

		timeConstant = rawConfig.getTimeConstant();
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public RateExceededResponse getRateExceededResponse() {
		return rateExceededResponse;
	}

	public double getRequestsPerTimeUnit() {
		return requestsPerTimeUnit;
	}

	public double getNormalizedRate(TimeUnit normalizeOn) {
		double rateInMs = requestsPerTimeUnit / timeUnit.millisecondsDuration;
		return rateInMs * normalizeOn.millisecondsDuration;
	}

	public Double getTimeConstant() {
		return timeConstant;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setRateExceededResponse(RateExceededResponse rateExceededResponse) {
		this.rateExceededResponse = rateExceededResponse;
	}

	public void setRequestsPerTimeUnit(double requestsPerTimeUnit) {
		this.requestsPerTimeUnit = requestsPerTimeUnit;
	}

	public void setTimeConstant(Double timeConstant) {
		this.timeConstant = timeConstant;
	}

	@Override
	public String toString() {
		return "RateLimit [timeUnit=" + timeUnit + ", rateExceededResponse=" + rateExceededResponse
				+ ", requestsPerTimeUnit=" + requestsPerTimeUnit + ", timeConstant=" + timeConstant + "]";
	}

}
