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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.hazelcast.nio.DataSerializable;

public class EMARateHistory implements Serializable, DataSerializable {
	private static final long serialVersionUID = 3528562524920197424L;

	private double lastRate;
	private long lastTime;

	public double getLastRate() {
		return lastRate;
	}

	public void setLastRate(double lastRate) {
		this.lastRate = lastRate;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	@Override
	public void readData(DataInput input) throws IOException {
		lastRate = input.readDouble();
		lastTime = input.readLong();
	}

	@Override
	public void writeData(DataOutput output) throws IOException {
		output.writeDouble(lastRate);
		output.writeLong(lastTime);
	}

}
