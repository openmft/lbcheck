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
package com.adi.amf.lbcheck.probe;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

public class FtpsProbe implements ProbeInterface {
	private Logger log = LogManager.getLogger(getClass().getName());
	private String serviceName;
	private String ftpHost;
	private int ftpPort;
	private String ftpUser;
	private String ftpCred;
	private String text;

	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		ftpHost = p.getStringProperty(ProbeConstants.HOST);
		ftpPort = p.getIntProperty(ProbeConstants.PORT);
		ftpUser = p.getStringProperty(ProbeConstants.USER);
		ftpCred = p.getStringProperty(ProbeConstants.CRED);
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
		FTPSClient client = null;
		try {
			client = new FTPSClient();
			//client.setKeyManager(keyManager);
			//client.setHostnameVerifier(newHostnameVerifier);
			//client.setTrustManager(trustManager);
			client.connect(ftpHost, ftpPort);
			log.debug(serviceName + " connected");
			int reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				throw new RuntimeException(serviceName + "- ftp connect failed");
			}
			if (!client.login(ftpUser, ftpCred)) {
				throw new RuntimeException(serviceName + "- ftp login failed");
			}
			log.debug(serviceName + " logged in");
			client.enterLocalPassiveMode();
			client.quit();
			result = true;
			text = "ftp/s logged in";
			log.debug(serviceName + " logged out");
		} catch (Exception e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				client.disconnect();
			} catch (Exception ignore) {
			}
		}
		return result;
	}

	@Override
	public String getDetails() {
		return text;
	}
}


