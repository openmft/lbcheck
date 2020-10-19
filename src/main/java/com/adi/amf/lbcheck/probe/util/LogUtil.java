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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {
	private static Logger log =  LogManager.getLogger(LogUtil.class);
	private final static String DATE_FORMAT = "YYYY/MM/DD HH:mm:ss.SSS";
	
	private static boolean useLog4j;
	static {
		useLog4j = Boolean.parseBoolean(System.getProperty(ProbeConstants.SERVER_USE_LOG4J));
	}
	
	public static void warn(Object obj, String text) {
		if (useLog4j) {
			log.warn(obj.getClass().getName() + ": " + text);
		} else {
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
			String val = df.format(System.currentTimeMillis()) + " WARN "+ obj.getClass().getName() + ": " + text;
			System.err.println(val);
		}
	}
	
	public static void warn(Object obj, String text, Throwable e) {
		if (useLog4j) {
			log.warn(obj.getClass().getName() + ": " + text, e);
		} else {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(os);
			e.printStackTrace(pw);
			pw.close();
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
			String val = df.format(System.currentTimeMillis()) + " WARN " 
					+ obj.getClass().getName() + ": " + text + ProbeConstants.NL + os.toString();
			System.err.println(val);
		}
	}
	public static void info(Object obj, String text) {
		if (useLog4j) {
			log.info(obj.getClass().getName() + ": " + text);
		} else {
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
			String val = df.format(System.currentTimeMillis()) + " INFO "+ obj.getClass().getName() + ": " + text;
			System.out.println(val);
		}
	}
}
