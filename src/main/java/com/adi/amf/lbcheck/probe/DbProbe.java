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
Purpose: Module to check DB availability

Parameters:	If module is named DB, the parameters are as follows.

DB-probe-class = bns.ph.monitoring.hc.DbProbe 
DB-user		= sfg
DB-password = *********
DB-url		= jdbc:db2://cs4cstersfgdbs.glbnft.bns:60001/SFGDB
DB-sql		= SELECT current date FROM sysibm.sysdummy1

Copyright IBM 2018
**********************************************************************************/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbProbe implements ProbeInterface {
	private String serviceName;
	private String dbUrl;
	private String dbUser;
	private String dbCred;
	private String dbSql;
	private Connection con = null;
	private PreparedStatement ps = null;
	private String text;
	
	public void init(MonitorProperties p)  {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		dbUrl = p.getStringProperty(ProbeConstants.URL);
		dbUser = p.getStringProperty(ProbeConstants.USER);
		dbCred = p.getStringProperty(ProbeConstants.CRED);
		dbSql = p.getStringProperty(ProbeConstants.SQL);
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
		ResultSet rs = null;
		try {
			if (con == null || con.isClosed()) {
				Class.forName("com.ibm.db2.jcc.DB2Driver");
				String value = AesUtil.decrypt(dbCred, ProbeConstants.BLAH);
				con = DriverManager.getConnection (dbUrl, dbUser, value);
				ps = con.prepareStatement(dbSql);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				text = rs.getString(1);
			}
			result = true;
		} catch (Exception e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
			try {
				ps.close();
			} catch (Exception ignore) {
			}			
			try {
				con.close();
			} catch (Exception ignore) {
			}
			con = null;
		} finally {
			try {
				rs.close();
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

