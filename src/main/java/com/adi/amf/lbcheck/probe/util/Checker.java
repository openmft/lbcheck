/*
##################################################################################
# License: MIT
# Copyright 2018 Agile Data Inc
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of 
# this software and associated documentation files (the "Software"), to deal in 
# the Software without restriction, including without limitation the # rights to 
# use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of 
# the Software, and to permit persons to whom the Software is furnished to do so, 
# subject to the following conditions:
# The above copyright notice and this permission notice shall be included in 
# all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
# PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE # AUTHORS OR COPYRIGHT 
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
# CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
# OR THE USE OR OTHER DEALINGS IN  # THE SOFTWARE.
##################################################################################
*/
package com.adi.amf.lbcheck.probe.util;

import java.util.HashMap;
import java.util.Map;

public class Checker implements Runnable {

	private boolean runFlag;
	private int interval;
	private Map<String,ProbeInterface> probes; 
	private Map<String, String> status;
	private boolean overallStatus;
	
	public Checker(Map<String,ProbeInterface> probes, int interval) {
		this.probes = probes;
		this.interval = interval;
		this.status = new HashMap<String, String>();
	}
	
	public void shutdown() {
		runFlag = false;
	}
	
	public boolean getStatus() {
		return overallStatus;
	}
	
	public Map<String,String> getStatusDetails() {
		return status;
	}

	public void run() {
		LogUtil.info(this, "checker started");
		runFlag = true;
		while(runFlag) {
			try {
				Thread.sleep(interval);
				int count = 0;
				for (String probe : probes.keySet()) {
					ProbeInterface pi = probes.get(probe);
					boolean result = pi.probe();
					status.put(probe, result + "/" + pi.getDetails());
					if (! result) {
						overallStatus = false;
						count++;
					}
				}
				overallStatus = count == 0;
				LogUtil.info(this, "overall status: " + overallStatus);
			} catch (Throwable e) {
				LogUtil.warn(this, "checker failed", e);
			}
		}
		LogUtil.info(this, "shutting down");
	}
}
