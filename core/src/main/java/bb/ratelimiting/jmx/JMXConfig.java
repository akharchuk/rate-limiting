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
package bb.ratelimiting.jmx;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class JMXConfig implements Serializable {
	private static final long serialVersionUID = 5843885834863157401L;

	private String statsDomainPrefix;
	private String configDomainPrefix;
	private String serverName;

	public String getStatsDomainPrefix() {
		return statsDomainPrefix;
	}

	public String getConfigDomainPrefix() {
		return configDomainPrefix;
	}

	public String getServerName() {
		return serverName;
	}

	public void setStatsDomainPrefix(String statsDomainPrefix) {
		this.statsDomainPrefix = statsDomainPrefix;
	}

	public void setConfigDomainPrefix(String configDomainPrefix) {
		this.configDomainPrefix = configDomainPrefix;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
