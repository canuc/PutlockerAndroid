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

import com.putlocker.upload.Constants;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;

public class RegisterHttpRequest extends CookiePersistantHttpRequest {

	public static final String USERNAME_FIELD = "username";
	public static final String EMAIL_FEILD = "email";
	public static final String PASSWORD_FIELD = "password";
	public static final String PASSWORD2_FIELD = "password2";
	public static final String REGISTER_ACCOUNT_FIELD = "register_account";
	public static final String REGISTER_ACCOUNT_VALUE = "Register";
	public static final String VOUCHER_FEILD = "voucher";
	private static final String ERROR_REGEXP = "message\\st_0\'>([^<]*?<)";
	private final HttpContext _context;

	private final String _username;
	private final String _password;
	private final String _email;
	private String _cauthToken;
	private String _authToken;
	private Pattern _patternError;

	private String _errorString;

	public RegisterHttpRequest(RequestCallback callback,
			CookiePersistantHttpRequest persistant, String username,
			String password, String email) {
		super(Constants.BASE_URL + Constants.SIGNUP_PAGE, callback, persistant);
		_context = new BasicHttpContext();
		_username = username;
		_password = password;
		_email = email;
	}

	@Override
	protected HttpResponse doHttpQuery() throws IOException {

		HttpClient client = new DefaultHttpClient(getConnectionParams());

		HttpPost postRequest = new HttpPost(getUrl());
		addAllCookies(postRequest);
		HttpResponse response = null;
		client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
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

		try {
			List<NameValuePair> valuePair = new LinkedList<NameValuePair>();
			valuePair.add(new BasicNameValuePair(USERNAME_FIELD, _username));
			valuePair.add(new BasicNameValuePair(PASSWORD_FIELD, _password));
			valuePair.add(new BasicNameValuePair(PASSWORD2_FIELD, _password));
			valuePair.add(new BasicNameValuePair(REGISTER_ACCOUNT_FIELD,
					REGISTER_ACCOUNT_VALUE));
			valuePair.add(new BasicNameValuePair(EMAIL_FEILD, _email));
			valuePair.add(new BasicNameValuePair(VOUCHER_FEILD, ""));
			postRequest.setEntity(new UrlEncodedFormEntity(valuePair));
			response = client.execute(postRequest);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return response;
	}

	public String getErrorString() {
		return _errorString;
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);

		List<Cookie> cookies = getAllCookies();
		boolean success = false;
		for (Cookie setCookie : cookies) {
			if (setCookie.getName().equals("cauth")) {
				_cauthToken = setCookie.getValue();
				success = true;
			} else if (setCookie.getName().equals("auth")) {
				_authToken = setCookie.getValue();
				success = true;
			}
		}
		if (success == false) {
			// We are going to preform the new request,
			// then get all the
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
			throw new PutlockerException(ExceptionType.RegisterError);
		}
	}

	public String getAuthToken() {
		return _authToken;
	}

	public String getCAuthToken() {
		return _cauthToken;
	}

	@Override
	protected HttpContext getContext() {
		// TODO Auto-generated method stub
		return _context;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getEmail() {
		return _email;
	}

}
