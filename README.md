# HTTP Rate Limiting Filter 

This is a generic HTTP rate limiting filter project. The filter supports multiple nodes/web modules and uses Hazelcast to store rate limiting statistics. 

## How to use

**Step 1**

Define HTTP filter in your web.xml


```
	<filter>
		<filter-name>SampleRateLimitingFilter</filter-name>
		<filter-class>bb.ratelimiting.RateLimitingFilter</filter-class>
		<init-param>
			<param-name>config</param-name>
			<param-value><![CDATA[
			{
				"enabled" : false,
				"persistConfig" : false,
				"hazelcast" : {
					"mapName" : "ratemapv2",
					"configMapName" : "rate-limiting-config-map"
				},
				"jmx" : {
					"statsDomainPrefix": "com.rim.bbdap",
					"configDomainPrefix": "com.rim.bbdap"
				}
				..................
			}]]></param-value>
		</init-param>
	</filter>

	<filter-mapping>
		<filter-name>SampleRateLimitingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```


**Step 2**

You can define rate limiting settings either in web.xml or in the external file passed in ratelimit.config.file system property.

Hazelcast cluster will be initialized either based on the filter setting in web.xml or HZ_FILE system property.





## Author(s)

* Adam Skubel
* Andriy Kharchuk
* Dejan Petronijevic


## Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.