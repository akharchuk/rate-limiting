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
 
const e = 2.71828;
var timePassed : number; // time passed between calls
const timeConstant = 50; // sampling frequency, the goal is to rate limit  10 requests/1 second

let lastRate = 0; // previously recorded rate, we start with zero
let duration = 0 ; // elapsed time
let frequency = 80; // initial simulated API invocation frequence, in this case it is one request every 80 ms


console.log("Time passed" +  "," + "Rate");

for(var i=1; i<20; i++){
	timePassed = frequency;
	duration += timePassed;
	lastRate = calculateRate(timeConstant, lastRate, timePassed);
	console.log(duration + "," + lastRate);
}

i--;

for(; i>0;i--){
	timePassed = frequency* (20-i);
	duration += timePassed;
	lastRate = calculateRate(timeConstant, lastRate, timePassed);
	console.log(duration + "," + lastRate);
}


function calculateRate(timeConstant: number, lastRate: number, timePassed: number) : number {
	timePassed = timePassed>1 ? timePassed : 1;
	let alpha : number = 1.0 - e**(-timePassed / timeConstant);
	return lastRate + alpha * ((1.0 / timePassed) - lastRate);
}