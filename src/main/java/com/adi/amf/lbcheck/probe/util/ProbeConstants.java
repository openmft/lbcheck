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

public class ProbeConstants {
	
	
	public final static String PROBE_CLASS = "probe-class";
	public final static String PROBE_NAME = "probe-name";
	
	public final static String HOST = "host";
	public final static String PORT = "port";
	public final static String USER = "user";
	public final static String URL = "url";
	public final static String SQL = "sql";
	public final static String COMMAND = "command";
	public final static String VERBOSE = "verbose";
	public final static String PROTOCOL = "protocol";
	public final static String AES = "aes";
	public final static String NL = "\n";
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public final static String EQUALS = "=";
	public final static String COLON_SPACE = ": ";
	
	
	public final static String USE_SSL = "use-ssl";
	public final static String CRED = "cred";
	public final static String CRED_CRED = "pk-cred";
	
	public final static String SERVER_PORT = "server-port";
	public final static String SERVER_BACKLOG = "server-backlog";
	public final static String SERVER_CONTEXT = "server-context";
	public final static String SERVER_INTERVAL = "server-interval";
	public final static String SERVER_DYNAMIC_READ = "dynamic-read";
	public final static String SERVER_USE_LOG4J = "server-use-log4j";

	public final static String SERVER_KEYSTORE = "server-keystore";

	public final static String PROBE_LIST = "probe-list";
	public final static String PROBE_LIST_DELIM = ",";
	
	public final static String DEFAULT_KEYSTORE = "healthcheck.jks";
	

	public final static String CHANNEL = "channel";
	public final static String MANAGER = "manager";
	public final static String QUEUE = "queue";
	public final static String BLAH = "_not_Laughing@2";
	
	public final static String ERROR_MISSING_CLASS = "property probe-class is missing for probe: ";
	public final static String SERVICE_PARAM = "service";
	public final static String TIMEOUT = "timeout";
}
