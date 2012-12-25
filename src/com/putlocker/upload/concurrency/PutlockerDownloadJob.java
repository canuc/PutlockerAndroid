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

package com.putlocker.upload.concurrency;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebStorage.Origin;

import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerUpDownloadJob;

public class PutlockerDownloadJob extends PutlockerUpDownloadJob implements Parcelable {
	public String _fileName = "";
	public Long _fileSize = 0l;
	public String url = "";
	private String _fileLocation;
	private String _originalFileLocation;
	private int _id = 0;
	
	public Vector<Cookie> cookies;
	public DownloadType type = DownloadType.DownloadTypeFile;
	
	
	public long downloadedFileSize = 0;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PutlockerDownloadJob other = (PutlockerDownloadJob) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	private static final String DOWNLOAD_URL_KEY = "url";
	public static final String DOWNLOAD_STATUS_KEY = "status";
	private static final String DOWNLOAD_FILENAME_KEY = "filename";
	private static final String DOWNLOAD_FILE_SIZE_KEY = "filesize";
	private static final String DOWNLOAD_TYPE_KEY = "type";
	private static final String DOWNLOAD_FILE_LOCATION = "filelocation";
	private static final String DOWNLOAD_ORIGINAL_FILE_LOCATION = "orginalFileLocation";
	
	
	public enum DownloadType {
		DownloadTypeStream,
		DownloadTypeFile;
		
		public static DownloadType getTypeForInt(int i) {
			return i == DownloadType.DownloadTypeFile.ordinal() ? DownloadTypeFile : DownloadTypeStream;
		}
	}
	
	public PutlockerDownloadJob(Cursor c)
	{
		super(c,PutlockerUpDownloadJob.DOWNLOAD_JOB);
	}
	public PutlockerDownloadJob()
	{
		super (PutlockerUpDownloadJob.DOWNLOAD_JOB);
	}
	

	
	@Override
	public String getTableName() {
		return "downloads";
	}

	
	@Override
	public int getId() {
		return  _id;
	}
	
	@Override
	public List<String> getKeys() {
		List<String> keyList = new ArrayList<String>();
		keyList.add(DOWNLOAD_FILE_SIZE_KEY);
		keyList.add(DOWNLOAD_FILENAME_KEY);
		keyList.add(DOWNLOAD_URL_KEY);
		keyList.add(DOWNLOAD_STATUS_KEY);
		keyList.add(DOWNLOAD_TYPE_KEY);
		keyList.add(DOWNLOAD_FILE_LOCATION);
		keyList.add(DOWNLOAD_ORIGINAL_FILE_LOCATION);
		return keyList;
	}
	
	@Override
	public String getValueForKey(String key) {
		String value = null;
		if ( key.equals(DOWNLOAD_FILE_SIZE_KEY)) {
			value = String.valueOf(_fileSize);
		} else if ( key.equals(DOWNLOAD_FILENAME_KEY)) {
			value = _fileName;
		} else if ( key.equals(DOWNLOAD_STATUS_KEY)) {
			value = getStatus().getValue();
		} else if ( key.equals(DOWNLOAD_URL_KEY)) {
			value = url;
		} else if ( key.equals(DOWNLOAD_TYPE_KEY)) {
			value = String.valueOf(type.ordinal());
		} else if ( key.equals(DOWNLOAD_FILE_LOCATION)) {
			value = _fileLocation;
		} else if ( key.equals(DOWNLOAD_ORIGINAL_FILE_LOCATION)) {
			value = _originalFileLocation;
		}
		return value;
	}
	
	@Override
	public void setValueForKey(String key, String value) {
		if ( key.equals(DOWNLOAD_FILE_SIZE_KEY)) {
			_fileSize = Long.valueOf(value);
		} else if ( key.equals(DOWNLOAD_FILENAME_KEY)) {
			_fileName = value;
		} else if ( key.equals(DOWNLOAD_STATUS_KEY)) {
			setStatus(value);
		} else if ( key.equals(DOWNLOAD_URL_KEY)) {
			url = value;
		} else if ( key.equals(DOWNLOAD_TYPE_KEY)) {
			type = DownloadType.getTypeForInt(Integer.parseInt(value));
		} else if ( key.equals(DOWNLOAD_FILE_LOCATION) ) {
			_fileLocation = value;
		} else if ( key.equals(DOWNLOAD_ORIGINAL_FILE_LOCATION)) {
			_originalFileLocation = value;
		}
		return;
	}
	
	@Override
	public void parseResult(Cursor cursor) {
		int url_key = cursor.getColumnIndex(DOWNLOAD_URL_KEY);
		int status_column_key = cursor.getColumnIndex(DOWNLOAD_STATUS_KEY);
		int file_name_key = cursor.getColumnIndex(DOWNLOAD_FILENAME_KEY);
		int download_type_key = cursor.getColumnIndex(DOWNLOAD_TYPE_KEY);
		int file_size_index = cursor.getColumnIndex(DOWNLOAD_FILE_SIZE_KEY);
		int file_location_index = cursor.getColumnIndex(DOWNLOAD_FILE_LOCATION);
		int id_key = cursor.getColumnIndex(getIdKey());
		int original_file_location = cursor.getColumnIndex(DOWNLOAD_ORIGINAL_FILE_LOCATION);
		_id = cursor.getInt(id_key);
		url= cursor.getString(url_key); 
		setStatus(DownloadStatus.statusForString(cursor.getString(status_column_key)));
		_fileLocation = cursor.getString(file_location_index);
		_fileName = cursor.getString(file_name_key);
		type = DownloadType.getTypeForInt(Integer.valueOf(cursor.getString(download_type_key)));
		_fileSize = Long.valueOf(cursor.getString(file_size_index));
		_originalFileLocation = cursor.getString(original_file_location);
	}
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<PutlockerDownloadJob> CREATOR
    = new Parcelable.Creator<PutlockerDownloadJob>() {
		public PutlockerDownloadJob createFromParcel(Parcel in) {
		    return new PutlockerDownloadJob(in);
		}
		
		public PutlockerDownloadJob[] newArray(int size) {
		    return new PutlockerDownloadJob[size];
		}
	};

	private PutlockerDownloadJob(Parcel in) {
		super(PutlockerUpDownloadJob.DOWNLOAD_JOB);
		_id = in.readInt();
		_fileName = in.readString();
		url = in.readString();
		_fileSize = in.readLong();
		type = in.readInt() == PutlockerDownloadJob.DownloadType.DownloadTypeFile.ordinal() ? PutlockerDownloadJob.DownloadType.DownloadTypeFile : PutlockerDownloadJob.DownloadType.DownloadTypeStream;
		
		int numberOfCookies = in.readInt();
		cookies = new Vector<Cookie>();
		for (int i = 0; i < numberOfCookies; i++) {
			BasicClientCookie cookie = new BasicClientCookie(in.readString(),in.readString());
			cookie.setDomain(in.readString());
			cookies.add(cookie);
		}
		
		_fileLocation = in.readString();
		_originalFileLocation = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_id);
		dest.writeString(_fileName);
		dest.writeString(url);
		dest.writeLong(_fileSize);
		
		dest.writeInt(type.ordinal());
		dest.writeInt(cookies.size());
		
		for (Cookie cookie: cookies) {
			dest.writeString(cookie.getName());
			dest.writeString(cookie.getValue());
			dest.writeString(cookie.getDomain());
		}
		
		dest.writeString( _fileLocation == null ? "" : _fileLocation );
		dest.writeString( _originalFileLocation );
	}
	
	@Override
	public String getName() 
	{
		return _fileName;
	}
	
	public Long getFileSize()
	{
		return _fileSize;
	}
	
	public boolean isAutoIncrement()
	{
		return true;
	}
	
	public String getFileLocation() 
	{
		return _fileLocation;
	}
	
	@Override
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setFileLocation(String location)
	{
		_fileLocation = location;
	}
	
	public String getOriginalFileLocation()
	{
		return _originalFileLocation;
	}
	
	public void setOriginalFileLocation(String originalFileLocation) 
	{
		_originalFileLocation = originalFileLocation;
	}
	
	@Override
	public int getProgressForDownload(long downloadProgress) {
		return  (int) Math.ceil(((double) downloadProgress / (double) _fileSize) * (double) 100.0);
	}


	@Override
	public int getProgressForDownload() {
		return  (int) Math.ceil(((double) downloadedFileSize / (double) _fileSize) * (double) 100.0);
	}
}
