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
package com.adi.amf.lbcheck.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adi.amf.lbcheck.probe.util.AesUtil;
import com.adi.amf.lbcheck.probe.util.Checker;
import com.adi.amf.lbcheck.probe.util.LogUtil;
import com.adi.amf.lbcheck.probe.util.MonitorProperties;
import com.adi.amf.lbcheck.probe.util.ProbeConstants;
import com.adi.amf.lbcheck.probe.util.ProbeInterface;

@WebServlet("/healthcheck")
public class LbCheckerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Object SYNCLOCK = new Object();
	private int interval;
	private Map<String,ProbeInterface> probes; 
	private Checker checker;

	@PostConstruct
	public void init() throws ServletException {
		try {
			LogUtil.info(this, "initialization starting");
			probes = new HashMap<String, ProbeInterface>();
			MonitorProperties serviceProperties = new MonitorProperties(new File("properties/hc.properties"));
			interval = Integer.parseInt(serviceProperties.getProperty(ProbeConstants.SERVER_INTERVAL)) * 1000;
			String probeNames = serviceProperties.getProperty(ProbeConstants.PROBE_LIST);
			String[] list = probeNames.split(ProbeConstants.PROBE_LIST_DELIM);
			for (int i=0; i<list.length; i++) {
				String probeName = list[i].trim();
				LogUtil.info(this, "initializing probe: " + probeName);
				MonitorProperties probeProperties = serviceProperties.getProbeProperties(probeName);
				String clazz = probeProperties.getProperty(ProbeConstants.PROBE_CLASS);
				if (clazz == null) {
					LogUtil.warn(this, ProbeConstants.ERROR_MISSING_CLASS + probeName);
					throw new Exception(ProbeConstants.ERROR_MISSING_CLASS + probeName);
				} else {
					clazz = clazz.trim();
					LogUtil.info(this, "loading probe class: " + clazz);
					ProbeInterface probe = (ProbeInterface) Class.forName(clazz).newInstance();
					probe.init(probeProperties);
					probes.put(probeName, probe);
				}
			}
			LogUtil.info(this, "initialization completed");
		} catch (Exception e) {
			LogUtil.warn(this, "initialization failed", e);
			throw new ServletException("initialization failed", e);
		}				
		if (checker == null) {
			checker = new Checker(probes, interval);
			Thread t = new Thread(checker);
			t.setDaemon(true);
			t.start();
		}
	}
		
	@PreDestroy
	public void destroy() {
		LogUtil.info(this, "shutdown requested");
		checker.shutdown();
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		try {
			// synchronize the initialization so it is only performed once, and only by one thread
			if (checker == null) {
				synchronized (SYNCLOCK) {
					if (checker == null) {
						init();
						
					}	
				}
			}
			String service = request.getParameter(ProbeConstants.SERVICE_PARAM);
			LogUtil.info(this,"Service parameter received:"+service);
			String val = request.getParameter(ProbeConstants.AES);
			if (val != null) {
				response.getWriter().println(AesUtil.encrypt(val, ProbeConstants.BLAH)+ProbeConstants.NL);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				boolean vflag = Boolean.parseBoolean(request.getParameter(ProbeConstants.VERBOSE));
				if (checker.getStatus()) {
					response.setStatus(HttpServletResponse.SC_OK);
					if (service!=null || vflag) {
						printDetails(service,response);
					}
				} else {
					response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
					if (vflag) {
						printDetails(service,response);
					}
				}
			}
		} catch (Exception e) {
			LogUtil.warn(this, "get operation failed", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void printDetails(String service,HttpServletResponse resp) throws IOException {
		Map<String,String> map = checker.getStatusDetails();
		if (service!=null && service.length()!=0) {
			resp.getWriter().println(service + ProbeConstants.COLON_SPACE + map.get(service));
		} else {
			for (String key : map.keySet()) {
				resp.getWriter().println(key + ProbeConstants.COLON_SPACE + map.get(key));
			}
		}
		
	}
}
