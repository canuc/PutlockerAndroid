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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.location.Address;
import android.util.Log;

import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.concurrency.interfaces.PutlockerTransferResponder;
import com.putlocker.upload.concurrency.interfaces.StopableRequest;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.exception.PutlockerException.ExceptionType;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;

public class PutlockerFileDownload extends FollowingHttpRequest implements StopableRequest{

	private final HttpContext context;
	private final PutlockerTransferResponder _responder;
	private final File _outFile;
	private Pattern urlPattern;
	private boolean canceled = false;
	private PutlockerDownloadJob _job;
	private PutlockerFileDownload _alias;
	private PutlockerFileDownload _nextJob;
	private Long downloadResume = null;
	private boolean shouldResume = false;
	private String originalUrl;
	private static final int READ_BUFFER_SIZE = 50*1024;// 50KB buffer
	
	public PutlockerFileDownload(PutlockerDownloadJob job,PutlockerTransferResponder responder, RequestCallback callback, File outputFile, PutlockerFileDownload alias) {
		super(job.url,  callback);
		context = new BasicHttpContext();
		_job = job;
		addCookiesFromJob(job);
		_responder = responder;
		_alias = alias;
		_outFile = outputFile;
		originalUrl = job.url;
	}
	
	protected PutlockerFileDownload(PutlockerDownloadJob job,PutlockerTransferResponder responder, RequestCallback callback, File outputFile, PutlockerFileDownload alias,boolean shouldDoStart) {
		this(job,responder,callback,outputFile);
		shouldResume = shouldDoStart;
	}
	
	public PutlockerFileDownload(PutlockerDownloadJob job,PutlockerTransferResponder responder, RequestCallback callback, File outputFile) 
	{
			this(job,responder,callback,outputFile,null);
	}
	
	public PutlockerDownloadJob getJob()
	{
		PutlockerDownloadJob job = _job;
		
		if ( _alias != null) {
			job = _alias.getJob();
		}
		
		return job;
	}
	
	protected void addCookiesFromJob(PutlockerDownloadJob job)
	{
		addAllCookies(job.cookies);
	}
	
	@Override
	protected HttpContext getContext() {
		return context;
	}
	
	@Override
	protected void addHttpHeaders(HttpRequest req)
	{
		super.addHttpHeaders(req);
		if ( shouldResume && _job.downloadedFileSize > 0 ) {
			// append to the start url - if we need to
			req.setHeader("Range", "bytes="+_job.downloadedFileSize+"-");
		}
		
	}
	
	@Override
	protected void processResponse(HttpResponse response) throws IOException, PutlockerException
	{
		super.processResponse(response);
		
		if (_job.type == PutlockerDownloadJob.DownloadType.DownloadTypeFile ) {
			processDownload(response);
		} else {
			processStream(response);
		}
		
	}
	
	@Override
	protected HttpResponse doHttpQuery() throws IOException, PutlockerException 
	{
		return super.doHttpQuery();
	}
	
	protected void processStream(HttpResponse response) throws IOException 
	{
		int retries = 0;
		if ( urlPattern == null )
		{
			urlPattern = Pattern.compile("url=\"(.+?)\"");
		}
		
		HttpEntity entity = response.getEntity();
		
		Matcher matcher = urlPattern.matcher(getStringFromEntity(entity));
		
		if ( matcher.find()) {
			String group = matcher.group(0);
			PutlockerDownloadJob job= new PutlockerDownloadJob();
			job.cookies = _job.cookies;
			job.setId(_job.getId());
			group = group.substring(5,group.length()-1);
			job.url = getSanitizedLocation(group);
			job._fileName= _job._fileName;
			job._fileSize = _job._fileSize;
			job.type = PutlockerDownloadJob.DownloadType.DownloadTypeFile;
			PutlockerFileDownload download = new PutlockerFileDownload(job, _responder, null, _outFile,this,true);
			_nextJob = download;
			download.run();
			
			
		}
	}
	
	protected void processDownload(HttpResponse response) throws IOException, PutlockerException
	{
		synchronized (this) {
			canceled = false;
		}
		
		PutlockerDownloadJob job = getJob();
		//Long contentLength = Long.parseLong(headers[0].getValue());
		HttpEntity entity = response.getEntity();
		
		Header [] header = response.getHeaders("Content-Length");
		
		// get the total file length
		Header [] xcontentlengthheader = response.getHeaders("X-Content-Length");
		
		int lastProgress = 0;
		Long contentLength;
		
		if ( xcontentlengthheader.length == 1 ) {
			contentLength = Long.parseLong(xcontentlengthheader[0].getValue());
		} else {
			contentLength = Long.parseLong(header[0].getValue());
		}
		
		job._fileSize = contentLength;
		
		_responder.setPutlockerJobStatus(job, DownloadStatus.JobStarted);
		
		// We are going to append onto the download if required as well we are going 
		FileOutputStream boStream = new FileOutputStream(_outFile,shouldResume && _job.downloadedFileSize > 0 );
		InputStream is = entity.getContent();
		FlushedInputStream bis = new FlushedInputStream(is);
		
		try {
			byte[] ret = new byte[READ_BUFFER_SIZE];
			boolean done = false;
			long bytes= job.downloadedFileSize;
			
	
			while (!done ) 
			{
				int read = bis.read(ret);
				
				if (read == -1) {
					done = true;
					break;
				}
				synchronized (this) {
					if ( canceled == true ) {
						setStatus(RequestStatus.RequestStatusCanceled);
						_lastGet.abort();
						break;
					}
				}
				
				bytes += (long) read;
				boStream.write(ret,0,read);
				boStream.flush();
				job.downloadedFileSize = bytes;
				int currProgress = job.getProgressForDownload(bytes);
				if ( lastProgress != job.getProgressForDownload() ) {
					_responder.setDownloadJobProgress(job, bytes,contentLength);
					lastProgress = job.getProgressForDownload();
				}
			
			}
		} finally {
			boStream.close();
			bis.close();
			is.close();
			
			
		}
		
	}

	@Override
	public void stopRequest() {
		if ( _nextJob != null ) {
			_nextJob.stopRequest();
		} else {
			synchronized(this){
				canceled = true;
			}
		}
	}

}
