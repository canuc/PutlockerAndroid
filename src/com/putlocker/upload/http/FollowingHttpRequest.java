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
import java.net.URLEncoder;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.net.Uri;

import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;

public abstract class FollowingHttpRequest extends CookiePersistantHttpRequest {
	private boolean hasRedirected = false;
	private String _baseUrlIfRelative;
	protected boolean doSecond()
	{
		return true;
	}
	protected boolean getRedirected()
	{
		return hasRedirected;
	}
	
	protected String getBaseHost()
	{
		return _baseUrlIfRelative;
	}
	
	public FollowingHttpRequest(String url, RequestCallback callback,
			CookiePersistantHttpRequest lastCookieStore) {
		super(url, callback, lastCookieStore);
		
		Uri uri = Uri.parse(url);
		if ( uri.getHost() == null ) {
			throw new RuntimeException("NULL HOST");
		}
		_baseUrlIfRelative = "http://"+uri.getHost();
	}
	
	public FollowingHttpRequest(String url, RequestCallback callback) {
		this(url,callback,null);
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);
		int responseCode = response.getStatusLine().getStatusCode();
		/**
		 * We are going to add in all the super.processResponse(response);
		 */
		
		if ( responseCode >= 300 && responseCode < 400 ) {
			
			Header[] location = response.getHeaders("Location");
			
			/*
			 * If the length of the location array is 1 then we know we are redirected
			 */
			if ( location.length == 1) {
				String newUrl = location[0].getValue();
				newUrl = getSanitizedLocation(newUrl);
				Uri parsedUri = Uri.parse(newUrl);
				if ( parsedUri.getScheme() == null ) {
					newUrl = _baseUrlIfRelative + newUrl;
				}
				_url = newUrl;
				hasRedirected = true;
				if ( doSecond() ) {
					goingToFollowUrl(_url);
					throw new PutlockerException(ExceptionType.Redirect);
				}
			} else {
				throw new PutlockerException(ExceptionType.ParseError);
			}
		}
	}
	
	
	protected void goingToFollowUrl(String url)
	{
		
	}
	protected static String getSanitizedLocation(String location) throws UnsupportedEncodingException
	{
		int locationOfQueryParams = location.lastIndexOf('?');
		String finalLocation = location;
		if ( locationOfQueryParams != -1 ) {
			String queryParams = location.substring(locationOfQueryParams+1);
			String beforeQueryParams = location.substring(0, locationOfQueryParams+1);
			StringTokenizer tokener = new StringTokenizer(queryParams,"&");
			String totalQueryString = "";
			// Go through all the current tokens
			for (int currTokenNum = 0; currTokenNum < tokener.countTokens(); currTokenNum++ ) {
				if ( currTokenNum != 0 ) {
					totalQueryString += "&";
				}
				String currToken = tokener.nextToken();
				int indexOfEquals = currToken.indexOf("=");
				if ( indexOfEquals != -1 ) {
					String varName = currToken.substring(0,indexOfEquals);
					String queryValue = URLEncoder.encode(currToken.substring(indexOfEquals+1, currToken.length()), "UTF-8");
					totalQueryString += varName + "=" +queryValue;
				} else {
					totalQueryString += currToken;
				}
			}
			finalLocation = beforeQueryParams + totalQueryString;
		}
		
		return finalLocation;
	}
	
	

}
