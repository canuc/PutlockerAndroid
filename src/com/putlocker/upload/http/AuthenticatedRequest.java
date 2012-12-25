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

import org.apache.http.protocol.HttpContext;

import com.putlocker.upload.Constants;
import com.putlocker.upload.storage.AuthStorage;

public abstract class AuthenticatedRequest extends FollowingHttpRequest {

	public AuthenticatedRequest(String url,AuthStorage storage, RequestCallback callback)
	{
		this(url,storage,callback,null);
	}
	
	public AuthenticatedRequest(String url,AuthStorage storage, RequestCallback callback , CookiePersistantHttpRequest cookieStore)
	{
		super(url,callback,cookieStore);
		addCookiesFromAuth(storage);
	}
	
	protected void addCookiesFromAuth(AuthStorage storage) {
		/**
		 * Add all the cookies for the request
		 */
		addCookie(Constants.CAUTH_COOKIE,
				storage.getValueForKey(AuthStorage.AUTH_CAUTHTOKEN));
		addCookie(Constants.AUTH_COOKIE,
				storage.getValueForKey(AuthStorage.AUTH_AUTHCODE));
	}

}
