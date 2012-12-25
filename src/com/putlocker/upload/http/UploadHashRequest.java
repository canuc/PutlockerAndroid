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

import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.putlocker.upload.Constants;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;
import com.putlocker.upload.storage.AuthStorage;

public class UploadHashRequest extends AuthenticatedRequest {
	private static Pattern uploadHashPattern;
	private static Pattern uploadFolderIdPattern;
	private final HttpContext _context;
	private String _uploadHash;
	private String _fileId;
	private String _folderHash;
	public UploadHashRequest(AuthStorage creds,RequestCallback callback,String folderHash) {
		super(Constants.BASE_URL+Constants.UPLOAD_FORM + (folderHash== null ? "" : "?folder=" + folderHash), creds, callback);
		_context = new BasicHttpContext();
	}

	@Override
	protected HttpContext getContext() {
		return _context;
	}
	
	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);
		String responseString = getStringFromEntity(response.getEntity());
		
		if (uploadHashPattern == null ) {
			uploadHashPattern = Pattern.compile("auth_hash':'([^\']*?\')");
		}
		
		Matcher match = uploadHashPattern.matcher(responseString);
		if( match.find()) {
			if (uploadFolderIdPattern == null ) {
				uploadFolderIdPattern = Pattern.compile("upload_folder':'([^\']*?\')");
			}
			if (_folderHash != null ) {
				Matcher currIdHash = uploadFolderIdPattern.matcher(responseString);
				
				if ( currIdHash.find() ) {
					_fileId = currIdHash.group(1);
					_fileId = _fileId.substring(0,_fileId.length()-1);
				}
			}
			String uploadHash = match.group(1);
			uploadHash = uploadHash.substring(0,uploadHash.length() - 1);
			
			_uploadHash = uploadHash;
		} else {
			throw new PutlockerException(ExceptionType.ParseError);
		}
	}
	
	public String getUploadHash()
	{
		return _uploadHash;
	}
	
	public String getFolderHash()
	{
		return _fileId;
	}
}
