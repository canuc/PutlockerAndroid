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
package com.putlocker.upload.storage;

import com.putlocker.upload.concurrency.interfaces.StopableRequest;

import android.database.Cursor;
import android.os.Parcelable;

public abstract class PutlockerUpDownloadJob extends Persistable implements Parcelable{
	public static final int DOWNLOAD_JOB = 1;
	public static final int UPLOAD_JOB = 2;
	private int _jobType; 
	private DownloadStatus _status;
	
	public enum DownloadStatus {
		DetailsRequesting("rq"),
		JobStarted("st"),
		JobPaused("dp"),
		JobError("de"),
		JobSucess("su"),
		JobCancelled("ca");
		
		String statusString;
		
		DownloadStatus(String status) 
		{
			statusString = status;
		}
		
		public String getValue()
		{
			return statusString;
		}
		
		public static DownloadStatus statusForString(String str) {
			if ( str.equals(DetailsRequesting.getValue())) {
				return DetailsRequesting;
			}else if ( str.equals(JobError.getValue())) {
				return JobError;
			} else if ( str.equals(JobPaused.getValue())){
				return JobPaused;
			} else if ( str.equals(JobStarted.getValue())) {
				return JobStarted;
			} else if ( str.equals(JobSucess.getValue())) {
				return JobSucess;
			}
			return JobError;
		}
	};
	
	protected PutlockerUpDownloadJob(int jobType) {
		super();
		_jobType = jobType;
		_status = DownloadStatus.DetailsRequesting;
	}
	
	protected PutlockerUpDownloadJob(Cursor c, int jobType)
	{
		this(jobType);
	}
	
	public int getJobType()
	{
		return _jobType;
	}
	
	public int getGlobalId() {
		int id = getId();
		
		if ( _jobType == DOWNLOAD_JOB ) {
			id = (Integer.MAX_VALUE / 2) + id;
		}
		
		return id;
	}
	
	public DownloadStatus getStatus()
	{
		return _status;
	}
	
	public void setStatus(DownloadStatus status)
	{
		_status = status;
	}
	
	protected void setStatus(String status)
	{
		_status = DownloadStatus.statusForString(status);
	}
	
	public abstract String getName();
	
	public abstract int getProgressForDownload(long downloadProgress);

	public abstract int getProgressForDownload();
	
	
}
