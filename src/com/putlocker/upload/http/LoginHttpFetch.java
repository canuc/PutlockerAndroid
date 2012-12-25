/**
 * Putlocker Android - Putlocker scraper for Android 
 *
 * Author: Julian Haldenby (j.haldenby@gmail.com)
 *
 *  This file is part of Putlocker Android.
 *
 * Putlocker Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Putlocker Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Putlocker Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.putlocker.upload.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.putlocker.upload.Constants;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;

public class LoginHttpFetch extends CookiePersistantHttpRequest {

	public static final String NAME_FIELD = "user";
	public static final String PASS_FIELD = "pass";
	public static final String CAPTCHA_FIELD = "captcha_code";
	public static final String LOGIN_FIELD = "login_submit";
	public static final String REMEMBER_FIELD = "remember";
	private static final String ERROR_REGEXP = "message\\st_0\'>([^<]*?<)";

	private HttpContext _context = null;
	private String _username;
	private String _password;
	private String _captcha;
	private String _cauthToken;

	private String authToken;
	private Pattern _patternError;

	private String _errorString;

	public LoginHttpFetch(CookiePersistantHttpRequest store,
			RequestCallback request, String username, String password,
			String Captcha) {
		super(Constants.BASE_URL + Constants.AUTHENTICATE_PAGE, request, store);
		_context = new BasicHttpContext();
		_username = username;
		_password = password;
		_captcha = Captcha;
	}

	public String getErrorString() {
		return _errorString;
	}

	@Override
	protected HttpContext getContext() {
		return _context;
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);
		List<Cookie> cookies = getAllCookies();
		boolean success = false;

		for (Cookie setCookie : cookies) {
			if (setCookie.getName().equals("auth")) {
				authToken = setCookie.getValue();
				success = true;
			} else if (setCookie.getName().equals("cauth")) {
				_cauthToken = setCookie.getValue();
			}
		}
		if (success == false) {
			// We are going to perform the new request,
			// then get the response
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode >= 300 && responseCode < 400) {
				Header[] location = response.getHeaders("Location");

				/*
				 * If the length of the location array is 1 then we know we are
				 * redirected
				 */
				if (location.length == 1) {
					String newUrl = location[0].getValue();
					_url = newUrl;
					HttpResponse resp = preformGetRequest();
					String entityString = getStringFromEntity(resp.getEntity());

					if (_patternError == null) {
						_patternError = Pattern.compile(ERROR_REGEXP);
					}

					Matcher matcher = _patternError.matcher(entityString);

					if (matcher.find()) {
						_errorString = matcher.group(1);
						_errorString = _errorString.substring(0,
								_errorString.length() - 1);
					}
				}
			}
			throw new PutlockerException(ExceptionType.LoginError);
		}
	}

	@Override
	protected HttpResponse doHttpQuery() throws IOException {
		HttpClient _client = new DefaultHttpClient(getConnectionParams());

		HttpPost postRequest = new HttpPost(getUrl());
		addAllCookies(postRequest);
		HttpResponse response = null;
		_client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		_client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				USER_AGENT);
		String cookieBuffer = "";
		boolean first = true;

		postRequest.setHeader("Origin", Constants.BASE_URL);
		postRequest.setHeader("Referer", getUrl());
		postRequest.setHeader("Connection", "keep-alive");
		postRequest.setHeader("Cache-Control", "max-age=0");
		postRequest
				.setHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		postRequest.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		try {
			List<NameValuePair> valuePair = new LinkedList<NameValuePair>();
			valuePair.add(new BasicNameValuePair(NAME_FIELD, _username));
			valuePair.add(new BasicNameValuePair(PASS_FIELD, _password));
			valuePair.add(new BasicNameValuePair(CAPTCHA_FIELD, _captcha));
			valuePair.add(new BasicNameValuePair(LOGIN_FIELD, "Login"));
			valuePair.add(new BasicNameValuePair(REMEMBER_FIELD, "1"));
			postRequest.setEntity(new UrlEncodedFormEntity(valuePair));
			response = _client.execute(postRequest);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return response;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getCAuthToken() {
		return _cauthToken;
	}

}
