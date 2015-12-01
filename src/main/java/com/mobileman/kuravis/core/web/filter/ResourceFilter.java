/*******************************************************************************
 * Copyright 2015 MobileMan GmbH
 * www.mobileman.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * ResourceFilter.java
 * 
 * Project: Kuravis
 * 
 * @author MobileMan GmbH
 * @date 12.11.2013
 * @version 1.0
 * 
 * (c) 2013 MobileMan GmbH, www.mobileman.com
 */

package com.mobileman.kuravis.core.web.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author MobileMan GmbH
 * 
 */
public class ResourceFilter extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceFilter.class);

	private final static String ESCAPE_PREFIX = "_escaped_fragment_";

	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private String phantomjsPath = null;
	private String phantomjsScriptPath = null;

	private String phantomjsLocalAddress = null;
	private String phantomjsLocalPort = null;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String uri = request.getRequestURI();
		String path = uri.substring(request.getContextPath().length());

		String queryString = request.getQueryString();
		if (queryString != null) {
			Map<String, String> pars = splitQuery(queryString);

			if (pars.containsKey(ESCAPE_PREFIX)) {
				// crawler detected
				if (LOG.isInfoEnabled()) {
					LOG.info("bot detected [" + path + "] [" + queryString
							+ "] " + request.getRequestURL().toString());
					LOG.info("Reconstructed URL: " + getURL(request));
				}
				/*
				 * String contextPath = request.getContextPath();
				 * if(StringUtils.isEmpty(contextPath)){ contextPath = "/"; }
				 */

				/*
				 * String localAddr = request.getLocalAddr(); if
				 * (localAddr.contains(":")) { // IPv6 localAddr = "[" +
				 * localAddr + "]"; }
				 */
				String scheme = "http"; // request.getScheme()
				// String url = scheme + "://" + localAddr + ":" +
				// request.getLocalPort();
				String url = scheme + "://" + getPhantomjsLocalAddress() + ":"
						+ getPhantomjsLocalPort();
				String tpath = path; // pars.get(ESCAPE_PREFIX);
				if (tpath.length() == 0 || tpath.charAt(0) != '/') {
					url += "/";
				}

				url += tpath + "?bot=1";

				for (String key : pars.keySet()) {
					if (ESCAPE_PREFIX.equals(key)) {
						continue;
					}
					url += "&";
					url += URLEncoder.encode(key, "UTF-8");
					url += "=";
					url += URLEncoder.encode(pars.get(key), "UTF-8");
				}

				LOG.info("PhantomJS Executing: " + url);

				try {
					// to debug phantomjs - first line in script is debugger;
					// enable --remote-debugger, open browser
					// http://127.0.0.1:9000/, click about:blank, go to Console
					// and write: __run()
					Process process = Runtime.getRuntime().exec(
							new String[] { getPhantomjsPath(), // "--remote-debugger-port=9000",
									getPhantomjsScriptPath(), url });

					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(process.getInputStream(),
									UTF8_CHARSET));

					String currentLine = null;
					StringBuilder stringBuilder = new StringBuilder();

					currentLine = bufferedReader.readLine();
					while (currentLine != null) {
						stringBuilder.append(currentLine);
						stringBuilder.append("\n");
						currentLine = bufferedReader.readLine();
					}
					int exitStatus = process.waitFor();

					String result = stringBuilder.toString();
					//result = result.replace("<base href=\"/\"", "<base href=\"http://www.kuravis.de/\"");
					//result = result.replace("<meta name=\"fragment\"", "<meta name=\"dummy\"");
					
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("text/html;charset=UTF-8");
					response.getOutputStream().write(
							result.getBytes(UTF8_CHARSET));
					LOG.info("PhantomJS Finishing: " + url);
				} catch (Throwable ex) {
					LOG.error("PhantomJS error [" + path + "] [" + queryString
							+ "]", ex);
				}
				return;
			}
		}

		String path1 = getServletContext().getRealPath(path);
		File f = new File(path1);
		boolean exists = f.exists();

		if (exists || path.equals("/") || path.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/");
		dispatcher.forward(request, response);
	}

	public static String getURL(HttpServletRequest req) {

		String scheme = req.getScheme(); // http
		String serverName = req.getServerName(); // hostname.com
		int serverPort = req.getServerPort(); // 80
		String contextPath = req.getContextPath(); // /mywebapp
		String servletPath = req.getServletPath(); // /servlet/MyServlet
		String pathInfo = req.getPathInfo(); // /a/b;c=123
		String queryString = req.getQueryString(); // d=789

		// Reconstruct original requesting URL
		StringBuffer url = new StringBuffer();
		url.append(scheme).append("://").append(serverName);

		if ((serverPort != 80) && (serverPort != 443)) {
			url.append(":").append(serverPort);
		}

		url.append(contextPath).append(servletPath);

		if (pathInfo != null) {
			url.append(pathInfo);
		}
		if (queryString != null) {
			url.append("?").append(queryString);
		}
		return url.toString();
	}

	private String getPhantomjsProp(String key, String def) {
		try {
			Properties p = new Properties();
			InputStream is = getClass().getResourceAsStream(
					"/phantomjs/phantomjs.properties");
			if (is != null) {
				p.load(is);
				return p.getProperty(key, def);
			}
		} catch (Exception e) {
			LOG.error("Error load " + key + " from phantomjs.properties", e);
		}
		return null;
	}

	private String getPhantomjsPath() {
		if (this.phantomjsPath == null) {
			this.phantomjsPath = getPhantomjsProp("path",
					"/usr/local/phantomjs/bin/phantomjs");
		}
		return this.phantomjsPath;
	}

	private String getPhantomjsLocalAddress() {
		if (this.phantomjsLocalAddress == null) {
			this.phantomjsLocalAddress = getPhantomjsProp("local.address",
					"127.0.0.1");
		}
		return this.phantomjsLocalAddress;
	}

	private String getPhantomjsLocalPort() {
		if (this.phantomjsLocalPort == null) {
			this.phantomjsLocalPort = getPhantomjsProp("local.port", "8080");
		}
		return this.phantomjsLocalPort;
	}

	private String getPhantomjsScriptPath() {

		if (this.phantomjsScriptPath == null) {
			ClassLoader classLoader = ResourceFilter.class.getClassLoader();
			File classpathRoot = new File(classLoader.getResource("").getPath());

			this.phantomjsScriptPath = new File(
					classpathRoot.getAbsolutePath(), "/phantomjs/phantom.js")
					.getAbsolutePath();
		}

		return this.phantomjsScriptPath;
	}

	private static Map<String, String> splitQuery(String query) {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			if (idx > 0) {
				try {
					query_pairs
							.put(URLDecoder.decode(pair.substring(0, idx),
									"UTF-8"), URLDecoder.decode(
									pair.substring(idx + 1), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return query_pairs;
	}

}
