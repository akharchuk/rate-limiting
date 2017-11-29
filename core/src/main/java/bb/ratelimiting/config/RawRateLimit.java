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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RawRateLimit implements Serializable {
	private static final long serialVersionUID = -277037430744355553L;

	@JsonProperty(value = "rate", required = true)
	private String rateDescriptor;

	@JsonProperty(value = "rateExceededResponse", required = true)
	private String rateExceededResponseDescriptor;

	@JsonProperty(value = "timeConstant", required = true)
	private Double timeConstant;

	public String getRateDescriptor() {
		return rateDescriptor;
	}

	public void setRateDescriptor(String rateDescriptor) {
		this.rateDescriptor = rateDescriptor;
	}

	public String getRateExceededResponseDescriptor() {
		return rateExceededResponseDescriptor;
	}

	public void setLimitExceedResponse(String rateExceededResponseDescriptor) {
		this.rateExceededResponseDescriptor = rateExceededResponseDescriptor;
	}

	public void setTimeConstant(Double timeConstant) {
		this.timeConstant = timeConstant;
	}

	public Double getTimeConstant() {
		return timeConstant;
	}
}
