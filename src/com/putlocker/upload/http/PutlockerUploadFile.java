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
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.putlocker.upload.Constants;
import com.putlocker.upload.concurrency.interfaces.PutlockerTransferResponder;
import com.putlocker.upload.concurrency.interfaces.StopableRequest;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;
import com.putlocker.upload.storage.PutlockerUploadJob;

public class PutlockerUploadFile extends AbstractHttpPostRequest implements StopableRequest {
	public static final String FILE_UPLOAD_FILENAME = "Filename";
	public static final String FILE_UPLOAD_SESSION = "session";
	public static final String FILE_UPLOAD_DO_CONVERT = "do_convert";
	public static final String FILE_UPLOAD_EXT = "fileext";
	public static final String FILE_UPLOAD_FOLDER = "folder";
	public static final String FILE_UPLOAD_AUTH_HASH = "auth_hash";
	public static final String FILE_UPLOAD_FILE_DATA = "Filedata";
	public static final String FILE_UPLOAD_UPLOAD_KEY = "Upload";
	public static final String FILE_UPLOAD_FOLDER_ID = "upload_folder";
	private int MIN_EVENT_UPLOAD = 50*1024; // MIN UPDATE CHUNK 50 KB
	
	private String _session;
	private String _authHash;
	private File _fileToUpload;
	private String _folder;
	private HttpContext _context;
	private PutlockerTransferResponder _responder;
	private PutlockerUploadJob _job;
	private String _fileId = null;
	private long lastProgress = 0;
	
	
	public PutlockerUploadFile(PutlockerUploadJob job, RequestCallback callback,PutlockerTransferResponder transferResponder) {
		super(Constants.UPLOAD_DOMAIN + Constants.UPLOAD_FILE_LOCATION, callback);
		_session = job._sessionId;
		_fileToUpload = new File(job.getFileLocation());
		_authHash = job._uploadHash;
		_folder = "/";
		_context = new BasicHttpContext();
		_responder = transferResponder;
		_job = job;
		_fileId = null;
	}

	@Override
	public void transferred(long num) {
		// If we have the last 
		if ( lastProgress + MIN_EVENT_UPLOAD <= num || num >= _job.getFileSize() )
		{
			_job.setTotalUploaded(num);
			_responder.setDownloadJobProgress(_job, num, _job.getFileSize());
			lastProgress = num;
		}
	}
	
	

	@Override
	protected List<String> getAllParts() {
		List<String> listOfKeys = new LinkedList<String>();
		listOfKeys.add(FILE_UPLOAD_FILENAME);
		listOfKeys.add(FILE_UPLOAD_SESSION);
		listOfKeys.add(FILE_UPLOAD_DO_CONVERT);
		listOfKeys.add(FILE_UPLOAD_EXT);
		listOfKeys.add(FILE_UPLOAD_AUTH_HASH);
		listOfKeys.add(FILE_UPLOAD_UPLOAD_KEY);
		listOfKeys.add(FILE_UPLOAD_FOLDER);
		listOfKeys.add(FILE_UPLOAD_FILE_DATA);
		if ( _fileId != null ) {
			listOfKeys.add(FILE_UPLOAD_FOLDER_ID);
		}
		return listOfKeys;
	}
	
	@Override
	protected ContentBody getBodyPart(String key) throws UnsupportedEncodingException {
		if (key.equals(FILE_UPLOAD_FILENAME)) {
			return new StringBody(_fileToUpload.getName());
		} else if (key.equals(FILE_UPLOAD_SESSION)) {
			return new StringBody(_session);
		} else if (key.equals(FILE_UPLOAD_DO_CONVERT)) {
			return new StringBody("1");
		} else if ( key.equals(FILE_UPLOAD_EXT) ) {
			return new StringBody("*");
		} else if ( key.equals(FILE_UPLOAD_FOLDER) ) {
			return new StringBody(_folder);
		} else if ( key.equals(FILE_UPLOAD_UPLOAD_KEY)) {
			return new StringBody("Submit Query");
		} else if ( key.equals(FILE_UPLOAD_AUTH_HASH)) {
			return new StringBody(_authHash);
		} else if ( key.equals(FILE_UPLOAD_FILE_DATA)) {
			return new FileBody(_fileToUpload, "application/octet-stream");
		} else if (key.equals(FILE_UPLOAD_FOLDER_ID)) {
			return new StringBody(_fileId);
		}
		return null;
	}
	
	@Override
	public HttpResponse doHttpQuery() throws IOException, PutlockerException
	{
		if ( _responder != null ) {
			_responder.setPutlockerJobStatus(_job, DownloadStatus.JobStarted);
		}
		
		return super.doHttpQuery();
	}
	
	@Override
	protected void processResponse(HttpResponse response) throws IOException, PutlockerException
	{
		super.processResponse(response);
		String string = getStringFromEntity(response.getEntity());
	}

	@Override
	protected HttpContext getContext() {
		return _context;
	}

	@Override
	public void stopRequest() {
		setStatus(RequestStatus.RequestStatusCanceled);
		_postRequest.abort();
	}

}
