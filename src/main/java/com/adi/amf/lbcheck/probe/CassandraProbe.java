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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;

import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

/*********************************************************************************
Purpose: Module to check Cassandra Database

Parameters:	If module is named CAS, the parameters are as follows.

CAS-probe-class = com.adi.amf.lbcheck.CassandraProbe  # this class name
CAS-host	= 10.90.192.70			# host
CAS-port	= 9042					# port
CAS-sql 	= select * from event.events limit 1	# query to run (this returns data center name)
CAS-user	= user					# db user
CAS-cred	= na					# credentials (when supported)
CAS-use-ssl = false 				# use SSL flag	

**********************************************************************************/

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.WhiteListPolicy;

public class CassandraProbe implements ProbeInterface {

	private String serviceName;
	private String host;
	private int port;
	private String sql;
	private String user;
	private String cred;
	private boolean useSSL;
	private String text;
	private String timeout;
	
	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		host = p.getStringProperty(ProbeConstants.HOST);
		port = p.getIntProperty(ProbeConstants.PORT);
		sql = p.getStringProperty(ProbeConstants.SQL);
		timeout = p.getStringProperty(ProbeConstants.TIMEOUT);
		user = p.getProperty(ProbeConstants.USER); // optional
		cred = p.getProperty(ProbeConstants.CRED); // optional
		useSSL = Boolean.parseBoolean(p.getProperty(ProbeConstants.USE_SSL)); //optional
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
        Cluster cluster = null;
        Session session = null;
		try {
			SocketOptions socketOptions = new SocketOptions();
		    socketOptions.setConnectTimeoutMillis(Integer.parseInt(timeout));
			cluster = Cluster.builder()
                    .addContactPoints(InetAddress.getByName(host))
                    .withPort(port)
                    .withLoadBalancingPolicy(new WhiteListPolicy(
                            new RoundRobinPolicy(), Collections.singletonList(new InetSocketAddress(host, port))))
                    .withQueryOptions(new QueryOptions().setDefaultIdempotence(true))
                    .withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))
                    .withSocketOptions(socketOptions)
                    .build();
			 	session = cluster.connect();
				ResultSet rs = session.execute(sql); 
				Row row = rs.one();
				text = row.getString(1);
				result = true;
		} catch (Throwable e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				session.close();
			} catch (Throwable ignore) {
			}
			try {
		        cluster.close();
			} catch (Throwable ignore) {
			}
		}
		return result;
	}

	@Override
	public String getDetails() {
		return text;
	}
}

