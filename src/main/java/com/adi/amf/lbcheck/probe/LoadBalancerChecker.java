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

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adi.amf.lbcheck.probe.util.AesUtil;
import com.adi.amf.lbcheck.probe.util.Checker;
import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

@SuppressWarnings("ClassNewInstance")
public class LoadBalancerChecker {
	private Logger log = LogManager.getLogger(getClass().getName());

	private int portNo;
	private int backlog;
	private int interval;
	private Checker checker;
	private String context;
	private String keystore;
	private static Map<String,ProbeInterface> probes; 

	public void run() {
		try {
			// start monitor
			checker = new Checker(probes, interval);
			Thread t = new Thread(checker);
			t.start();
			// start https server
			InetSocketAddress address = new InetSocketAddress(portNo);
			HttpsServer server = HttpsServer.create(address, backlog);
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

			char[] cred = ProbeConstants.BLAH.toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream fis = new FileInputStream(keystore);
			ks.load(fis, cred);

			// setup the key manager factory
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("IbmX509");
			kmf.init(ks, cred);
			// setup the trust manager factory
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("IbmX509");
			tmf.init(ks);

			// setup the HTTPS context and parameters
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters parms) {
					try {
						SSLContext c = SSLContext.getDefault();
						SSLEngine engine = c.createSSLEngine();
						parms.setNeedClientAuth(false);
						parms.setCipherSuites(engine.getEnabledCipherSuites());
						parms.setProtocols(engine.getEnabledProtocols());
						SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
						parms.setSSLParameters(defaultSSLParameters);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
			});
			server.createContext(context, new MyHandler());
			server.setExecutor(null); 
			server.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class MyHandler implements HttpHandler {

		public void handle(HttpExchange t) throws IOException {
			log.debug("received ping from: " + t.getRemoteAddress().getHostName());
			try {
				String val  = (String)t.getRequestURI().getQuery();
				if (val != null && val.toLowerCase().contains(ProbeConstants.AES)) {
					doAes(t);
				} else {
					doCheck(t);
				}
			} catch (Exception e) {
				log.warn("could not write response", e);
			}
		}
	}
	
	private void doAes(HttpExchange t) throws Exception {
		int code = 200;
		String text = "missing value";
		String val  = (String)t.getRequestURI().getQuery();
		String[] vals = val.split(ProbeConstants.EQUALS);
		if (vals.length > 1) {
			text = vals[1];
		}
		String resp = text + ProbeConstants.COLON_SPACE + 
				AesUtil.encrypt(text, ProbeConstants.BLAH) + ProbeConstants.NL;
		t.sendResponseHeaders(code, resp.length());
		t.getResponseBody().write(resp.getBytes());
		t.getResponseBody().close();
		
	}
	
	private void doCheck(HttpExchange t) throws IOException {
		int code = 503;
		byte[] resp = new byte[0];
		boolean verbose = false;
		String val  = (String)t.getRequestURI().getQuery();
		if (val.contains(ProbeConstants.VERBOSE) && val.contains(ProbeConstants.TRUE)) {
			verbose = true;
		}
		log.debug("checking status");
		if (checker.getStatus()) {
			code = 200;
			if (verbose) {
				resp = printDetails(t);
			}
		} else {
			if (verbose) {
				resp = printDetails(t);
			}
		}					
		t.sendResponseHeaders(code, resp.length);
		t.getResponseBody().write(resp);
		t.getResponseBody().close();		
	}
	
	private byte[] printDetails(HttpExchange t) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Map<String,String> map = checker.getStatusDetails();
		for (String key : map.keySet()) {
			os.write((key + ProbeConstants.COLON_SPACE 
					+ map.get(key) + ProbeConstants.NL).getBytes());
		}	
		return os.toByteArray();
	}
	
	@SuppressWarnings("deprecation")
	public void init(String fname) {		
		probes = new HashMap<String, ProbeInterface>();
		try {
			MonitorProperties serviceProperties = new MonitorProperties(new File(fname));
			portNo = Integer.parseInt(serviceProperties.getProperty(ProbeConstants.SERVER_PORT));
			backlog = Integer.parseInt(serviceProperties.getProperty(ProbeConstants.SERVER_BACKLOG));
			interval = Integer.parseInt(serviceProperties.getProperty(ProbeConstants.SERVER_INTERVAL)) * 1000;
			context = serviceProperties.getProperty(ProbeConstants.SERVER_CONTEXT);
			keystore = serviceProperties.getProperty(ProbeConstants.SERVER_KEYSTORE, ProbeConstants.DEFAULT_KEYSTORE);
			String useLog4j = serviceProperties.getProperty(ProbeConstants.SERVER_USE_LOG4J, ProbeConstants.FALSE);
			System.getProperties().put(ProbeConstants.SERVER_USE_LOG4J, useLog4j);
			String serviceNames = serviceProperties.getProperty(ProbeConstants.PROBE_LIST);
			String[] list = serviceNames.split(ProbeConstants.PROBE_LIST_DELIM);
			for (int i=0; i<list.length; i++) {
				String probeName = list[i].trim();
				LogUtil.info(this, "probe name: " + probeName);
				MonitorProperties probeProperties = serviceProperties.getProbeProperties(probeName);
				String clazz = probeProperties.getProperty(ProbeConstants.PROBE_CLASS);
				if (clazz == null) {
					LogUtil.warn(this, ProbeConstants.ERROR_MISSING_CLASS + probeName);
					throw new Exception(ProbeConstants.ERROR_MISSING_CLASS + probeName);
				} else {
					clazz = clazz.trim();
					LogUtil.info(this, "probe class: " + clazz);
					ProbeInterface probe = (ProbeInterface) Class.forName(clazz).newInstance();
					probe.init(probeProperties);
					probes.put(probeName, probe);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("fatal exception", t);
		}					
	}

	public static void main(String[] args) {
		LoadBalancerChecker hc = new LoadBalancerChecker();
		if (args.length == 0) {
			hc.init("hc.properties");
		} else {
			hc.init(args[0]);			
		}
		hc.run();
	}
}
