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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.putlocker.upload.Constants;
import com.putlocker.upload.exception.PutlockerException;

public class RequestCaptchaLocationHttpFetch extends CookiePersistantHttpRequest {
	private CookieStore _cookieStore = null;
	private HttpContext _context = null;
	/**
	 * We are going to need to get the captcha image location
	 */
	private String _captchaImage = null;
	private HttpClient _client = null;
	private static Pattern _pattern = null;
	private static Pattern _replaceAmp = null;
	
	public RequestCaptchaLocationHttpFetch( RequestCallback callback, final String url) {
		super(url,callback);
		_context = new BasicHttpContext();

		if (_cookieStore != null) {
			_context.setAttribute(ClientContext.COOKIE_STORE, _cookieStore);
		}
	}

	protected HttpResponse doFetch() throws IOException {
		_client = new DefaultHttpClient(getConnectionParams());
		HttpGet getRequest = new HttpGet(getUrl());
		HttpResponse response = null;
		
		try {
			response = _client.execute(getRequest, _context);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected HttpResponse doHttpQuery() throws IOException {
		// TODO Auto-generated method stub
		return doFetch();
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException, PutlockerException {
		super.processResponse(response);
		HttpEntity entity = response.getEntity();
		
		String responseText = getStringFromEntity(entity);
		/** 
		 * We are going to get the basic entity from
		 * the response text
		 */
		if ( _pattern == null ) {
			_pattern = Pattern.compile("(\"/include/captcha.php[^\"]*?\")",Pattern.MULTILINE);
		}
		Matcher matcher = _pattern.matcher(responseText);
		
		if ( matcher.find() ) {
			String url = matcher.group(0);
			url = Constants.BASE_URL + url.substring(1, url.length()-2);
			
			if ( _replaceAmp == null ) {
				_replaceAmp = Pattern.compile("&amp;");
			}
			
			Matcher replaceMatcher = _replaceAmp.matcher(url);
			
			// set our captcha image location
			_captchaImage = replaceMatcher.replaceAll("&");
			if ( _captchaImage == null ) {
				throw new PutlockerException(PutlockerException.ExceptionType.ParseError);
			}
		} else {
			throw new PutlockerException(PutlockerException.ExceptionType.ParseError);
		}
	}

	public CookieStore getCookieStore()
	{
		return _cookieStore;
	}
	
	public String getCaptchaLocation() {
		return _captchaImage;
	}
	
	protected HttpContext getContext() {
		return _context;
	}
}
