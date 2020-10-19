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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.adi.amf.lbcheck.probe.util.AesUtil;
import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

public class FtpProbe implements ProbeInterface {
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
		FTPClient client = null;
		try {
			client = new FTPClient();
			client.connect(ftpHost, ftpPort);
			int reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				throw new RuntimeException(serviceName + "- ftp connect failed");
			}
			String value = AesUtil.decrypt(ftpCred, ProbeConstants.BLAH);
			if (!client.login(ftpUser, value)) {
				throw new RuntimeException(serviceName + "- ftp login failed");
			}
			client.enterLocalPassiveMode();
			client.quit();
			result = true;
			text = "ftp logged in";
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


