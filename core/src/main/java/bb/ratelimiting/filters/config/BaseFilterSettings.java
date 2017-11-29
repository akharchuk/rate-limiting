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
package bb.ratelimiting.filters.config;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = JSONContentFilterSettings.class, name = "json-content"),
		@JsonSubTypes.Type(value = OriginFilterSettings.class, name = "origin"),
		@JsonSubTypes.Type(value = PathFilterSettings.class, name = "path"),
		@JsonSubTypes.Type(value = HeaderFilterSettings.class, name = "header") })
public class BaseFilterSettings implements Serializable {
	private static final long serialVersionUID = 8207174180864007857L;

	protected String type;

	@JsonProperty("httpMethods")
	protected List<String> httpMethods;

	public List<String> getHttpMethods() {
		return httpMethods;
	}

	public void setHttpMethods(List<String> httpMethods) {
		this.httpMethods = httpMethods;
	}

	@JsonIgnore
	public String getType() {
		return type;
	}

}
