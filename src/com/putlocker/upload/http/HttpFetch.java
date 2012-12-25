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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;


public abstract class HttpFetch implements Runnable {
	
	
	public static enum RequestStatus {
		RequestStatusIncomplete,
		RequestStatusSucess,
		RequestStatusCanceled,
		RequestStatusError
	};
	
	protected String _url = null;
	protected boolean _isDone;
	protected RequestStatus _status = RequestStatus.RequestStatusIncomplete;
	
	private RequestCallback _callback = null;
	public final static int MAX_RETRIES = 3;
	protected final String USER_AGENT="Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1";
	
	protected final int DEFAULT_TIMEOUT_CONNECTION = 30000;
	protected final int DEFAULT_SOCKET_TIMEOUT = 20000;
	
	protected HttpGet _lastGet;
	public HttpFetch(String url, RequestCallback callback) {
		_url = url;
		_callback = callback;
	}

	protected final String getUrl() {
		return _url;
	}

	/**
	 * Each run will contain the retry logic that is neccesary
	 */
	@Override
	public final void run() {
		HttpResponse response;
		int count = 0;
		boolean success = false;
		
		while (count < getMaxRetries() && !success && !isDone()) {
			try {
				response = doHttpQuery();
				
				if ( response != null ) {
					int responseCode = response.getStatusLine().getStatusCode();
					// We are going to handle redirects here and allow the user to continue
					if (( responseCode >= 200 && responseCode < 300 ) || responseCode == 302) {
						processResponse(response);
						success = true;
						break;
					} 
				} else {
					requestRetry();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				requestRetry();
				count++;
			} catch ( PutlockerException e) {
				
				if ( e.getExceptionType() == ExceptionType.LoginError) {
					success = false;
					break;
				} else if ( e.getExceptionType() == ExceptionType.RegisterError ) {
					success = false;
					break;
				} else if ( e.getExceptionType() == ExceptionType.Redirect ) {
					// do nothing
				} else {
					count++;
				}
				e.printStackTrace();
				// We are not going to retry if there is a putlocker exception
				success = false;
			}
			
		}
		if ( _status.equals(RequestStatus.RequestStatusIncomplete) ) {
			Log.e("JULIAN", "Request not completed yet: "+ String.valueOf(success));
			if ( success == false ) {
				setStatus(RequestStatus.RequestStatusError);
				requestFailure();
			} else {
				setStatus(RequestStatus.RequestStatusSucess);
				requestSuccess();
			}
		} else {
			Log.e("JULIAN", "Request sucess");
			if ( getStatus().equals(RequestStatus.RequestStatusSucess)) {	
				requestSuccess();
			} else if ( success == false ) {
				requestFailure();
			}
		}
		return;
	}

	protected String getStringFromEntity(HttpEntity entity) throws IOException {
		InputStream is = entity.getContent();
		BufferedInputStream bis = new BufferedInputStream(is);

		byte[] ret = new byte[2 * 1024];
		String parsedBuffer = new String();
		boolean done = false;

		while (!done) {
			int read = bis.read(ret);
			if (read == -1) {
				done = true;
				break;
			}
			String tmpString = new String(ret, 0, read);
			parsedBuffer += tmpString;

		}

		parsedBuffer += ret;

		is.close();

		return parsedBuffer;
	}
	
	protected byte [] getBytesFromEntity(HttpEntity entity) throws IOException {
		ByteArrayOutputStream boStream = new ByteArrayOutputStream();
		InputStream is = entity.getContent();
		FlushedInputStream bis = new FlushedInputStream(is);

		byte[] ret = new byte[2 * 1024];
		String parsedBuffer = new String();
		boolean done = false;

		while (!done) {
			int read = bis.read(ret);
			if (read == -1) {
				done = true;
				break;
			}
			boStream.write(ret,0,read);
			ret = new byte[2 * 1024];
		}
		is.close();
		return boStream.toByteArray();
	}

	protected void requestRetry() {
		if ( _callback != null ) {
			_callback.requestRetry(this);
		}
	}
	
	protected void requestSuccess() {
		if ( _callback != null ) {
			_callback.requestSuccess(this);
		}
	}
	
	 protected void requestFailure() {
		 if ( _callback != null ) {
				_callback.requestFailure(this);
			}
	 }
	 
	 protected HttpParams getConnectionParams()
	 {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, DEFAULT_TIMEOUT_CONNECTION);
		HttpConnectionParams.setSoTimeout(params, DEFAULT_SOCKET_TIMEOUT);
		return params;
	 }
	 /**
	  * This will preform the get request that needs to be completed
	  * @return the response object
	  * @throws IOException If there was an error in connection 
	  * @throws PutlockerException If there was a protocol exception
	  */
	protected final HttpResponse preformGetRequest() throws IOException, PutlockerException 
	{
		HttpClient client = new DefaultHttpClient(getConnectionParams());
		client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,false);
		String urlGotten = getUrl();
		HttpGet getRequest = new HttpGet(urlGotten);
		_lastGet = getRequest;
		/**
		 * This is fairly important to add all the necessary headers
		 */
		addHttpHeaders(getRequest);
		HttpResponse response = null;
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		try {
			response = client.execute(getRequest, getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * This will be a single query for a url
	 * 
	 * @return
	 * @throws IOException
	 *             If the connection cannot be established then an IOException
	 *             will be thrown
	 *             
	 * @throws PutlockerException 
	 * 			   If the error is a parse error or something application level
	 * 			   then this will throw
	 */
	protected HttpResponse doHttpQuery() throws IOException, PutlockerException 
	{
		return preformGetRequest();
	}
	
	/**
	 * This can be overriden to provide facilities to adding in to an
	 * http header
	 * 
	 * @param request
	 */
	protected void addHttpHeaders(HttpRequest request ) 
	{
	}
	
	protected int getMaxRetries()
	{
		return 3;
	}
	
	public final boolean isSucess()
	{
		return getStatus().equals(RequestStatus.RequestStatusSucess);
	}
	
	public final boolean isDone()
	{
		return _isDone;
	}
	
	public final RequestStatus getStatus()
	{
		return _status;
	}

	protected final void setStatus(RequestStatus status)
	{
		if ( !isDone() ) {
			_status = status;
			_isDone = true;
		}
	}
	protected abstract HttpContext getContext();
	/**
	 * This will process the response that we recieve for the query
	 * 
	 * @param response
	 *            the response that we need to proc
	 * @throws IOException
	 * @throws PutlockerException 
	 */
	protected abstract void processResponse(HttpResponse response)
			throws IOException, PutlockerException;
}
