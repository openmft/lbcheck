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

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

public class HttpProbe implements ProbeInterface {
	private String serviceName;
	private String httpUrl;
	private String text;

	public void init(MonitorProperties p) {
		serviceName = p.getStringProperty(ProbeConstants.PROBE_NAME);
		httpUrl = p.getStringProperty(ProbeConstants.URL);
	}
	
	public boolean probe() {
		text = "";
		boolean result = false;
		HttpURLConnection con = null;
		try {
			URL obj = new URL(httpUrl);
			if (obj.getProtocol().toLowerCase().equals("http")) {
				con = (HttpURLConnection) obj.openConnection();
			} else {
				 // Install the all-trusting trust manager
				 TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
		                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                    return null;
		                }
		                public void checkClientTrusted(X509Certificate[] certs, String authType) {
		                }
		                public void checkServerTrusted(X509Certificate[] certs, String authType) {
		                }
		            }
		        };
		 
				SSLContext sc = SSLContext.getInstance("SSL");
		        sc.init(null, trustAllCerts, new java.security.SecureRandom());
		        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		        
				HostnameVerifier allHostsValid = new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
			    };
			    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
				con = (HttpsURLConnection) obj.openConnection();
			}
			int code = con.getResponseCode();
			if (code == 200) {
				result = true;
			}
			text = "HTTP: " + String.valueOf(code);
		} catch (Exception e) {
			text = e.getMessage();
			LogUtil.warn(this, serviceName + " failed", e);
		} finally {
			try {
				con.disconnect();
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
