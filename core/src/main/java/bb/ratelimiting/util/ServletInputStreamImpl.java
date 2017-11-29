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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class ServletInputStreamImpl extends ServletInputStream {

	private InputStream is;

	public ServletInputStreamImpl(InputStream is) {
		this.is = is;
	}

	public int read() throws IOException {
		return is.read();
	}

	public boolean markSupported() {
		return false;
	}

	public synchronized void mark(int i) {
		throw new RuntimeException(new IOException("mark/reset not supported"));
	}

	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

}
