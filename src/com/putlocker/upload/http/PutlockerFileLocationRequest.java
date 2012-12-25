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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.util.Log;

import com.putlocker.upload.Constants;
import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.concurrency.PutlockerDownloadJob.DownloadType;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;

public class PutlockerFileLocationRequest extends FollowingHttpRequest {
	private final HttpContext _context;
	private final String _hashCode ;
	private final String UPLOAD_HASH_TOKEN_KEY = "hash";
	private final String CONFIRM_USER_KEY = "confirm";
	private final String CONFIRM_USER = "Continue as Free User";
	private static Pattern _pattern;
	private static Pattern _namePattern;
	private PutlockerDownloadJob _fileLocation;
	public PutlockerFileLocationRequest(String url, RequestCallback callback, CookiePersistantHttpRequest request, 
		String hashCode) {
		super(url, callback, request);
		_context = new BasicHttpContext();
		_hashCode = hashCode;
	}
	
	protected PutlockerFileLocationRequest(String url, RequestCallback callback)
	{
		super(url, callback, null);
		_context = new BasicHttpContext();
		_hashCode = null;
	}
	
	public PutlockerDownloadJob getJobLocation()
	{
		return _fileLocation;
	}

	@Override
	protected HttpContext getContext() {
		// TODO Auto-generated method stub
		return _context;
	}
	
	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);
		String resultString = getStringFromEntity(response.getEntity());
		
		processString(resultString);
	}
		
	protected void processString(String responseString) throws PutlockerException
	{
		if (_pattern == null) {
			_pattern = Pattern.compile("/get_file.php[^\"']*.?",Pattern.DOTALL);
		}

		Matcher matcher = _pattern.matcher(responseString);
		if ( matcher.find() ) {
			String url = matcher.group(0);
			
			String fileLocation = getBaseHost() + url.substring(0,url.length()-1);
			if ( _namePattern == null ) {
				_namePattern = Pattern.compile("<h1>[^<]*.?");
			}
			
			Matcher nameMatcher =_namePattern.matcher(responseString);
			String name = "";
			if ( nameMatcher.find() ) {
				name = nameMatcher.group(0);
				name = name.substring(4,name.length()-1);
			}
			
			// Now we have all the data needed to complete the download job,
			// we are going to do all the downloading
			_fileLocation = new PutlockerDownloadJob();
			_fileLocation.url = fileLocation;
			_fileLocation.cookies = getAllCookies();
			_fileLocation._fileName = name;
			_fileLocation.setOriginalFileLocation(getUrl());
			if (url.contains("stream")) {
				_fileLocation.type = DownloadType.DownloadTypeStream;
			} else {
				_fileLocation.type = DownloadType.DownloadTypeFile;
			}
			
		}else {
			throw new PutlockerException(ExceptionType.ParseError);
		}
	}
	
	protected HttpResponse doHttpQuery() throws IOException, PutlockerException
	{
		if ( !getRedirected() ) {
			return doFirstPost();
		} else {
			return preformGetRequest();
		}
	}
	
	protected HttpResponse doFirstPost() throws ClientProtocolException, IOException
	{
		HttpClient _client = new DefaultHttpClient(getConnectionParams());
		
		HttpPost postRequest = new HttpPost(getUrl());
		addAllCookies(postRequest);
		HttpResponse response = null;
		_client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,false);
		_client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		String cookieBuffer = "";
		boolean first = true;
	
		postRequest.setHeader("Referer",getUrl());
		postRequest.setHeader("Connection","keep-alive");
		postRequest.setHeader("Cache-Control", "max-age=0");
		postRequest.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		
		try {
			List<NameValuePair> valuePair = new LinkedList<NameValuePair>();
			valuePair.add(new BasicNameValuePair(UPLOAD_HASH_TOKEN_KEY, _hashCode));
			valuePair.add(new BasicNameValuePair(CONFIRM_USER_KEY, CONFIRM_USER));
			postRequest.setEntity(new UrlEncodedFormEntity(valuePair));
			response = _client.execute(postRequest);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
	    
		return response;
	}
	

}
