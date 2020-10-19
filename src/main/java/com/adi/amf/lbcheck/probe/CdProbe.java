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

import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;
import com.sterlingcommerce.cd.sdk.Node;

public class CdProbe implements ProbeInterface {

	private String serviceName;
	private String text;
	private String user;
	private String cred;
	private String ip;
//	private int port;
	private String protocol;
	private String command;
	
	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);	
		user = p.getStringProperty(ProbeConstants.USER);
		cred = p.getStringProperty(ProbeConstants.CRED);
		ip = p.getStringProperty(ProbeConstants.HOST);
//		port = p.getIntProperty(ProbeConstants.PORT);
		protocol = p.getStringProperty(ProbeConstants.PROTOCOL);
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
		Node n = null;
		try {
			n = new Node(ip, user, cred.toCharArray(), protocol);
			if (command != null) {
				n.execute(command);
			}
			text = n.getName();
			result = true;
		} catch (Exception e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				n.closeNode();
			} catch (Exception ignore) {
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
