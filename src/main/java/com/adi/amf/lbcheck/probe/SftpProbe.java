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

import com.adi.amf.lbcheck.probe.util.AesUtil;
import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

/*********************************************************************************
Purpose: Module to check SFTP server availability.  This module will log in and run
		 the list command.  Currently the Jscape library used by this module is not
		 approved by BNS.

Parameters:	If module is named SFTP, the parameters are as follows.  If the 
			private key value is "na" then use password for authentication.

SFTP-probe-class = bns.ph.monitoring.hc.SftpProbe 
SFTP-host	= CS4CSTERGTWYS01.bns
SFTP-port	= 40039
SFTP-user	= JONFTCUSTOMERE
SFTP-password 	 = tester
SFTP-private-key = na
#SFTP-private-key = /temp/JONFTCUSTOMERA_PrivateKey.ppk
SFTP-private-key-passphrase = test

Copyright IBM 2018
**********************************************************************************/

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class SftpProbe implements ProbeInterface {

	private String serviceName;
	private String host;
	private int port;
	private String user;
	private String cred;
	private String text;
//	private String pkey;
//	private String passphrase;

	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		host = p.getStringProperty(ProbeConstants.HOST);
		port = p.getIntProperty(ProbeConstants.PORT);
		user = p.getStringProperty(ProbeConstants.USER);
		cred = p.getStringProperty(ProbeConstants.CRED);
//		pkey  = PropertyUtil.getStringProperty(p, serviceName + ProbeConstants.PRIVATE_KEY);
//		passphrase = PropertyUtil.getStringProperty(p, serviceName + ProbeConstants.PRIVATE_KEY_PASSPHRASE);
	}

	public boolean probe() {
		text = "";
		boolean result = false;
		JSch jsch=new JSch();
		Session session= null;
		ChannelSftp sftp = null;
		try {
			session=jsch.getSession(user, host, port);
			String value = AesUtil.decrypt(cred, ProbeConstants.BLAH);
			session.setPassword(value);
			//session.setPassword(cred);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			Channel channel=session.openChannel("sftp");
		    channel.connect();
		    sftp = (ChannelSftp)channel;
		    text = "pwd: " + sftp.pwd();
		    sftp.quit();
			result = true;
		} catch (Exception e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				sftp.disconnect();		
			} catch (Exception e) {
			}
			try {
				session.disconnect();		
			} catch (Exception e) {
			}
		}
		return result;
	}

	@Override
	public String getDetails() {
		// TODO Auto-generated method stub
		return text;
	}
}


