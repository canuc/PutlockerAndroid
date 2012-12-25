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
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.http.post.CountingMultipartEntity;

public abstract class AbstractHttpPostRequest extends FollowingHttpRequest implements CountingMultipartEntity.ProgressListener{

	protected HttpPost _postRequest; 
	
	public AbstractHttpPostRequest(String url, RequestCallback callback) {
		super(url, callback);
	}
	
	public HttpResponse doHttpQuery() throws IOException, PutlockerException
	{
		CountingMultipartEntity entry = new CountingMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,this);
		List<String> allBodyParts = getAllParts();
		for ( String bodyPart : allBodyParts ) {
			entry.addPart(bodyPart, getBodyPart(bodyPart));
		}
		HttpClient client = new DefaultHttpClient(getConnectionParams());
		client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,false);
		String urlGotten = getUrl();
		HttpPost postRequest = new HttpPost(urlGotten);
		_postRequest = postRequest;
		postRequest.setEntity(entry);
		addHttpHeaders(postRequest);
		return client.execute(postRequest);
	}
	

	protected abstract List<String> getAllParts();
	
	protected abstract ContentBody getBodyPart(String key) throws UnsupportedEncodingException;
}
