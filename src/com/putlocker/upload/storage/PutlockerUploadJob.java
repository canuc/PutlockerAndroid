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

import java.util.LinkedList;
import java.util.List;

import com.putlocker.upload.concurrency.PutlockerDownloadJob;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class PutlockerUploadJob extends PutlockerUpDownloadJob {

	public static final String PUTLOCKER_UPLOAD_SIZE = "uploadSize";
	public static final String PUTLOCKER_TOTAL_UPLOADED = "totalUploaded";
	public static final String PUTLOCKER_FILE_NAME = "filename";
	public static final String PUTLOCKER_FILE_LOCATION = "filelocation";
	public static final String PUTLOCKER_UPLOAD_STATUS = "status";
	
	public static final String TABLE_NAME = "uploads";
	
	private Long _fileSize;
	private Long _totalUploaded = 0l;
	private String _fileLocation;
	private String _fileName;
	
	private int _id;
	
	public String _uploadHash; // This will not be persisted, just parcellized
	public String _sessionId;// This is the session identifier
	public String _folderId;
	
	protected PutlockerUploadJob(Cursor c) {
		super(c, PutlockerUpDownloadJob.UPLOAD_JOB);
	}

	public PutlockerUploadJob()
	{
		super( PutlockerUpDownloadJob.UPLOAD_JOB);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_id);
		dest.writeLong(_fileSize);
		dest.writeLong(_totalUploaded);
		dest.writeString(_fileLocation);
		dest.writeString(_fileName);
		dest.writeString(getStatus().getValue());
		if ( _uploadHash != null ) {
			dest.writeInt(1);
			dest.writeString(_uploadHash);
		} else {
			dest.writeInt(0);
		}
		
		if ( _sessionId != null ) {
			dest.writeInt(1);
			dest.writeString(_sessionId);
		} else {
			dest.writeInt(0);
		}
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public int getId() {
		return _id;
	}

	@Override
	public List<String> getKeys() {
		List<String> keys = new LinkedList<String>();
		keys.add(PUTLOCKER_FILE_LOCATION);
		keys.add(PUTLOCKER_FILE_NAME);
		keys.add(PUTLOCKER_UPLOAD_SIZE);
		keys.add(PUTLOCKER_TOTAL_UPLOADED);
		keys.add(PUTLOCKER_UPLOAD_STATUS);
		return keys;
	}

	@Override
	public String getValueForKey(String key) {
		String value = null;
		if ( key.equals(PUTLOCKER_FILE_LOCATION) ) {
			value = _fileLocation ;
		} else if ( key.equals(PUTLOCKER_UPLOAD_SIZE)) {
			value = String.valueOf(_fileSize) ;
		} else if ( key.equals(PUTLOCKER_TOTAL_UPLOADED)) {
			value = String.valueOf(_totalUploaded);
		} else if ( key.equals(PUTLOCKER_FILE_NAME)) {
			value = _fileName;
		} else if ( key.equals(PUTLOCKER_UPLOAD_STATUS)) {
			value = getStatus().getValue();
		}
		return value;
	}

	@Override
	public void setValueForKey(String key, String value) {
		if ( key.equals(PUTLOCKER_FILE_LOCATION) ) {
			_fileLocation = value;
		} else if ( key.equals(PUTLOCKER_UPLOAD_SIZE)) {
			_fileSize = Long.parseLong(value);
		} else if ( key.equals(PUTLOCKER_TOTAL_UPLOADED)) {
			_totalUploaded = Long.parseLong(value);
		} else if ( key.equals(PUTLOCKER_FILE_NAME)) {
			_fileName = value;
		} else if (key.equals(PUTLOCKER_UPLOAD_STATUS)) {
			setStatus(value);
		}
	}

	@Override
	public void parseResult(Cursor cursor) {
		int index_file_location = cursor.getColumnIndex(PUTLOCKER_FILE_LOCATION);
		int index_file_name = cursor.getColumnIndex(PUTLOCKER_FILE_NAME);
		int index_file_size = cursor.getColumnIndex(PUTLOCKER_UPLOAD_SIZE);
		int index_file_uploaded = cursor.getColumnIndex(PUTLOCKER_TOTAL_UPLOADED);
		int index_id = cursor.getColumnIndex(getIdKey());
		int index_file_status = cursor.getColumnIndex(PUTLOCKER_UPLOAD_STATUS);
		
		_fileName = cursor.getString(index_file_name);
		_totalUploaded = cursor.getLong(index_file_uploaded);
		_id = cursor.getInt(index_id);
		_fileSize = cursor.getLong(index_file_size);
		_fileLocation = cursor.getString(index_file_location);
		setStatus(cursor.getString(index_file_status));
		return;
	}
	
	public static final Parcelable.Creator<PutlockerUploadJob> CREATOR
    = new Parcelable.Creator<PutlockerUploadJob>() {
		public PutlockerUploadJob createFromParcel(Parcel in) {
		    return new PutlockerUploadJob(in);
		}
		
		public PutlockerUploadJob[] newArray(int size) {
		    return new PutlockerUploadJob[size];
		}
	};
	
	@Override 
	public boolean isAutoIncrement()
	{
		return true;
	}

	
	@Override 
	public void setId(int id) 
	{
		_id = id;
	}
	
	public void setTotalUploaded(long total)
	{
		_totalUploaded = total;
	}
	
	public void setFileLocation(String fileLocation) 
	{
		_fileLocation = fileLocation;
	}
	
	public void setName(String fileName)
	{
		_fileName = fileName;
	}
	
	public void setTotalFileSize(long fileSize)
	{
		_fileSize = fileSize;
	}
	
	public String getName()
	{
		return _fileName;
	}
	
	public String getFileLocation()
	{
		return _fileLocation;
	}
	
	public long getFileSize()
	{
		return _fileSize;
	}
	
	private PutlockerUploadJob(Parcel in) {
		super(PutlockerUpDownloadJob.UPLOAD_JOB);
		_id = in.readInt();
		_fileSize = in.readLong();
		_totalUploaded = in.readLong();
		_fileLocation = in.readString();
		_fileName = in.readString();
		setStatus(in.readString());
		if ( in.readInt() == 1 ) {
			_uploadHash = in.readString();
		}
		
		if ( in.readInt() == 1 ) {
			_sessionId = in.readString();
		}
	}

	@Override
	public int getProgressForDownload(long downloadProgress) {
		if ( _fileSize == 0 ) {
			return 0;
		} else {
			return  (int) Math.ceil(((double) downloadProgress / (double) _fileSize) * (double) 100.0);
		}
	}

	@Override
	public int getProgressForDownload() {
		if ( _fileSize == 0 ) {
			return 0;
		} else {
			return (int) Math.ceil(((double) _totalUploaded / (double) _fileSize) * (double) 100.0);
		}
	}

	
}
