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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;

public class PutlockerFileHashRequest extends PutlockerFileLocationRequest {

	public enum PutlockerDownloadRequestType {
		FileHash,
		FileLocation
	};
	private final HttpContext _context;
	private static Pattern _pattern;
	private static Pattern _extractPattern;
	private PutlockerDownloadRequestType _type = PutlockerDownloadRequestType.FileHash;
	private String hashToken;
	
	public PutlockerFileHashRequest(String url, RequestCallback callback) {
		super(url, callback);
		_context = new BasicHttpContext();
	}
	
	public String getRequestedUrl()
	{
		return getUrl();
	}
	
	@Override
	protected HttpResponse doHttpQuery() throws IOException, PutlockerException
	{
		return preformGetRequest();
	}
	
	@Override
	protected HttpContext getContext() {
		// TODO Auto-generated method stub
		return _context;
	}
	
	public String getHashToken()
	{
		return hashToken;
	}
	
	public PutlockerDownloadRequestType getType()
	{
		return _type;
	}
	
	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		// get the 
		processCookies(response.getHeaders(SET_COOKIE_HEADER));
		// We are going to get the response 
		HttpEntity ent = response.getEntity();
		String entityString = getStringFromEntity(ent);

		if ( _pattern == null ) {
			_pattern = Pattern.compile("value=\"[^\"]*?\"\\sname=\"hash\"",Pattern.MULTILINE);
		}
		
		Matcher matcher = _pattern.matcher(entityString);
		if ( matcher.find() ) {
			String url = matcher.group(0);
			int first_index = url.indexOf('\"');
			int second_index = url.indexOf('\"',first_index+1);
			
			if ( first_index != -1 && second_index != -1 ) {
				hashToken= url.substring(first_index+1, second_index);
			}else {
				throw new PutlockerException(ExceptionType.ParseError);
			}
		} else {
			_type = PutlockerDownloadRequestType.FileLocation;
			super.processString(entityString);
		}
	}

}
