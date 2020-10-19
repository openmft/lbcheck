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

import java.util.Hashtable;

import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;

public class MqProbe implements ProbeInterface {
	private Hashtable<String, Object> map;
	private String serviceName;
	private String mqHost;
	private int mqPort;
	private String mqUser;
	private String mqChannel;
	private String mqManager;
	private String mqQueue;
	private String text;
	
	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		mqHost = p.getStringProperty(ProbeConstants.HOST);
		mqPort = p.getIntProperty(ProbeConstants.PORT);
		mqUser = p.getStringProperty(ProbeConstants.USER);
		mqManager = p.getStringProperty(ProbeConstants.MANAGER);
		mqChannel = p.getStringProperty(ProbeConstants.CHANNEL);
		mqQueue = p.getStringProperty(ProbeConstants.QUEUE);
		map = new Hashtable<String,Object>();
		map.put(MQConstants.HOST_NAME_PROPERTY, mqHost);
		map.put(MQConstants.PORT_PROPERTY, mqPort);
		map.put(MQConstants.CHANNEL_PROPERTY, mqChannel);
		map.put(MQConstants.USER_ID_PROPERTY, mqUser);
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
		MQQueueManager qMgr = null;
		int openOptions = CMQC.MQOO_INQUIRE + CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED;
		try {
			qMgr = new MQQueueManager(mqManager, map);					
			MQQueue destQueue = qMgr.accessQueue(mqQueue, openOptions);
			text = "depth: " + String.valueOf(destQueue.getCurrentDepth());
			destQueue.close();
			result = true;
		} catch (Throwable e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				qMgr.disconnect();
			} catch (Exception ignore) {
			}
			try {
				qMgr.close();
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

