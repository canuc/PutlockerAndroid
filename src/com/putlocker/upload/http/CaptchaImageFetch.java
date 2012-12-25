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

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.putlocker.upload.exception.PutlockerException;

public class CaptchaImageFetch extends CookiePersistantHttpRequest {
	private HttpContext _context = null;
	private File _fileOutput ;
	private Bitmap _fileBmp;
	// if the out file is null then we will not use 
	// a cache image
	public CaptchaImageFetch (CookiePersistantHttpRequest cookies,String url,File outFile,RequestCallback callback)
	{
		super(url,callback,cookies);
		_context = new BasicHttpContext();
		_fileOutput = outFile;
	}
	
	@Override
	protected HttpContext getContext() {
		return _context;
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		// We are going to write the byteArray to the outfile
		HttpEntity ent = response.getEntity();
		byte [] output = getBytesFromEntity(ent);
		if (output != null && output.length > 0) {
			_fileBmp = BitmapFactory.decodeByteArray(output, 0, output.length);
		} else { 
			throw new IOException();
		}
	}
	
	@Override
	public int getMaxRetries()
	{
		return 2;
	}
	
	public Bitmap getCatchaBitmap()
	{
		return _fileBmp;
	}

}
