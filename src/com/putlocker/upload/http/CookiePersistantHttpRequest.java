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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;

import android.net.Uri;

import com.putlocker.upload.exception.PutlockerException;

public abstract class CookiePersistantHttpRequest extends HttpFetch {
	private Uri _uriHost; //!< This is the URI for this request
	private HashMap<String,Cookie> _cookieVector = new HashMap<String,Cookie>(); //!< This is the currentVector of cookies for this request
	protected final String SET_COOKIE_HEADER = "Set-Cookie";
	/**
	 * This constructor will initialize the sub request that is required
	 * @param url The url to get
	 * @param callback The response callback
	 */
	public CookiePersistantHttpRequest(String url, RequestCallback callback) {
		this(url,callback,null);
	}
	
	/**
	 * This constructor will initialize the sub request that is required
	 * 
	 * @param url The url to get
	 * @param callback The response callback
	 * @param lastCookieStore The last cookie store request that is too be used
	 */
	public CookiePersistantHttpRequest(String url, RequestCallback callback, CookiePersistantHttpRequest lastCookieStore)
	{
		super(url, callback);
		_uriHost = Uri.parse(url);
		if ( lastCookieStore != null ){
			addAllCookies(lastCookieStore.getAllCookies());
		}
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException, PutlockerException
	{
		processCookies(response.getHeaders(SET_COOKIE_HEADER));
	}
	
	
	protected void processCookies(Header[] headers) 
	{
		
		BrowserCompatSpec spec = new BrowserCompatSpec();
		for (Header currHeader:headers) {
			try {
			 List<Cookie> currCookie = spec.parse(currHeader,new CookieOrigin(_uriHost.getHost(), _uriHost.getPort() == -1 ? 80 : _uriHost.getPort(), "/*", false));
			addAllCookies(currCookie);
			} catch (MalformedCookieException e) 
			{
				e.printStackTrace();
			}
		}
		
		return;
	}
	
	@Override
	protected void addHttpHeaders(HttpRequest request ) 
	{
		addAllCookies(request);
	}
	
	protected void addCookie(String name, String value)
	{
		BrowserCompatSpec spec = new BrowserCompatSpec();
		_cookieVector.put(name,new BasicClientCookie(name, value));
	}
	
	protected void addAllCookies(HttpRequest request) 
	{
		BrowserCompatSpec spec = new BrowserCompatSpec();
		List<Header> headerVec = new Vector<Header>();
		if ( _cookieVector.size() > 0 ) {
			Vector<Cookie> cookieVec = getAllCookies();
			headerVec = spec.formatCookies(cookieVec);
		
			for ( Header currentHeader : headerVec ) {
				request.addHeader(currentHeader);
			}
		}
	}
	
	protected void addAllCookies(Collection<Cookie> cookies)
	{
		for( Cookie cookie :cookies ) {
		_cookieVector.put(cookie.getName(),cookie);
		}
	}
	
	public Vector<Cookie> getAllCookies()
	{
		Vector<Cookie> cookies = new Vector();
		Iterator<Map.Entry<String,Cookie>> it = _cookieVector.entrySet().iterator();
		while( it.hasNext() ) {
			cookies.add(it.next().getValue());
		}
		return cookies;
	}
	
	public String getCookie(String cookie)
	{
		if ( _cookieVector.containsKey(cookie)) 
		{
			return _cookieVector.get(cookie).getValue();
		}
		
		return null;
	}


}
