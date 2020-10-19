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

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

public class MonitorProperties extends Properties {

	private static final long serialVersionUID = 1L;

	public MonitorProperties() {
	}
	
	
	public MonitorProperties(File propertiesFile) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(propertiesFile);
			this.load(is);
		} catch (Exception e) {
			throw new RuntimeException("property file load failed", e);
		} finally {
			try {
				is.close();
			} catch (Exception ignore) {
			}
		}
	}
	
	public MonitorProperties getProbeProperties(String probeName) {
		MonitorProperties probeProperties = new MonitorProperties();
		probeProperties.put(ProbeConstants.PROBE_NAME, probeName);
		Enumeration<?> en = this.keys();
		while(en.hasMoreElements()) {
			String key = (String)en.nextElement();
			if (key.startsWith(probeName+"-")) {
				String val = this.getProperty(key);
				key = key.replaceAll(probeName+"-", "");
				probeProperties.put(key, val);
			}
		}
		return probeProperties;
	}
	
	public String getStringProperty(String propertyName) {
		String val = this.getProperty(propertyName);
		if (val == null || val.trim().equals("")) {
			throw new RuntimeException("required property not found: " + propertyName);
		} else {
			val = val.trim();
		}
		return val;
	}

	public int getIntProperty(String propertyName) {
		int result = -1;
		String val = this.getProperty(propertyName);
		if (val == null || val.trim().equals("")) {
			throw new RuntimeException("required property not found: " + propertyName);
		} else {
			result = Integer.parseInt(val);
		}
		return result;
	}
}
