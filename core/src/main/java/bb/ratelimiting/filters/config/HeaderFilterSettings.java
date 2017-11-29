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

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderFilterSettings extends BaseFilterSettings {
	private static final long serialVersionUID = 701138814018001489L;

	@JsonProperty(required = true)
	private String headerName;

	private String expression;

	@JsonProperty("extract")
	private int extractIndex;

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public int getExtractIndex() {
		return extractIndex;
	}

	public void setExtractIndex(int extractIndex) {
		this.extractIndex = extractIndex;
	}

}
