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
package bb.ratelimiting.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class StringUtil {

	public static String replace(String source, Properties properties) {
		HashMap<String, String> map = new HashMap<String, String>(properties.size());
		Iterator<Object> iter = properties.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			map.put(key, properties.getProperty(key));
		}
		return replace(source, map);
	}

	/**
	 * Replaces ${__} with a map according to what is provided.
	 * 
	 * @param source
	 * @param replacements
	 * @return
	 */
	public static String replace(String source, Map<String, String> replacements) {

		if (replacements == null)
			return source;
		if (replacements.size() == 0)
			return source;

		char[] ch = source.toCharArray();
		StringBuilder formatted = new StringBuilder();
		boolean skip = false;

		for (int i = 0; i < ch.length; i++) {

			if (i >= 1 && ch[i - 1] == '$' && ch[i] == '{') {
				// formatted.delete(i-1, i);
				StringBuilder rep = new StringBuilder();
				while (ch[++i] != '}') {
					rep.append(ch[i]);
				}
				String replace = replacements.get(rep.toString());
				if (replace == null)
					replace = "${" + rep.toString() + "}";
				formatted.append(replace);

			} else if (ch[i] == '$') {
				skip = true;
			} else {
				formatted.append(ch[i]);
			}

		}
		return formatted.toString();

	}
}
