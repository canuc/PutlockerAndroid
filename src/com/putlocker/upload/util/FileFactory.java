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

package com.putlocker.upload.util;

import java.io.File;
import java.text.DecimalFormat;

import android.os.Environment;


public class FileFactory {
	private File _fileChosen;
	private boolean mExternalStorageAvailable = false;
	private static final String DEFAULT_EXTERNAL_FOLDER = "putlocker";
	public static enum FileType {
		FILE_TYPE_VIDEO,
		FILE_TYPE_IMAGE,
		FILE_TYPE_TEXT,
		FILE_TYPE_AUDIO,
		FILE_TYPE_DOWNLOAD // THis is other
	};
	
	public FileFactory(String filename)
	{
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		if ( mExternalStorageAvailable && mExternalStorageWriteable ) {
			// We have external storage write to the putlocker dir
			File dir = Environment.getExternalStorageDirectory();
			File folder = new File(dir,DEFAULT_EXTERNAL_FOLDER);
			if (!folder.exists()) {
				folder.mkdir();
			}
			_fileChosen = new File(folder,filename);
		} else {
			mExternalStorageAvailable = false;
		}
		
	}
	
	
	public static boolean hasExternalWritableStorage()
	{
		String state = Environment.getExternalStorageState();
		boolean isStorageAvailable;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
			isStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
			isStorageAvailable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
			isStorageAvailable = false;
		}
		return isStorageAvailable;
	}
	
		
	public static FileType typeFromFileName(String filename)
	{
		filename = filename.toLowerCase();
		if (filename.equals("apk")) {
			return FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("txt") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("csv") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("xml") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("htm") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("html") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("php") ) {
			return FileType.FILE_TYPE_TEXT;
		} else if ( filename.equals("png") ) {
			return FileType.FILE_TYPE_IMAGE;
		} else if ( filename.equals("gif") ) {
			return FileType.FILE_TYPE_IMAGE;
		} else if ( filename.equals("jpg") ) {
			return FileType.FILE_TYPE_IMAGE;
		} else if ( filename.equals("jpeg") ) {
			return FileType.FILE_TYPE_IMAGE;
		} else if ( filename.equals("bmp") ) {
			return FileType.FILE_TYPE_IMAGE;
		} else if ( filename.equals("mp3") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("wav") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("ogg") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("mid") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("midi") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("amr") ) {
			return FileType.FILE_TYPE_AUDIO;
		} else if ( filename.equals("mpg") || filename.equals("mpeg") ) {
			return FileType.FILE_TYPE_VIDEO;
		} else if ( filename.equals("3gp") ) {
			return FileType.FILE_TYPE_VIDEO;
		} else if ( filename.equals("flv") ) {
			return FileType.FILE_TYPE_VIDEO;
		} else if ( filename.equals("zip") ) {
			return  FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("jar") ) {
			return  FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("zip") ) {
			return  FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("rar") ) {
			return  FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("gz") ) {
			return FileType.FILE_TYPE_DOWNLOAD;
		} else if ( filename.equals("ogg") ) {
			return FileType.FILE_TYPE_DOWNLOAD;
		} else if (filename.equals("avi")) {
			return  FileType.FILE_TYPE_VIDEO;
		} else if (filename.equals("mkv")) {
			return FileType.FILE_TYPE_VIDEO;
		} else if (filename.equals("mp4")) {
			return FileType.FILE_TYPE_VIDEO;
		} else if (filename.equals("f4v")) {
			return FileType.FILE_TYPE_VIDEO;
		} else if (filename.equals("crdownload")) {
			return FileType.FILE_TYPE_VIDEO;
		} 		
		return FileType.FILE_TYPE_DOWNLOAD;
	}
	
	public FileType fileNameToType(String name)
	{
		if ( name != null && name.length() >= 3) {
			int index = name.lastIndexOf('.');
			if ( index != -1 ) {
				String extension = name.substring(index+1);
				return typeFromFileName(extension);
			}
		}
		
		return FileType.FILE_TYPE_DOWNLOAD;
	}
	
	public static String fileExtensionFromName(String name) 
	{
		if ( name != null && name.length() >= 3) {
			int index = name.lastIndexOf('.');
			if ( index != -1 ) {
				return name.substring(index+1);
			}
		}
		return "";
	}
	
	public File getFile()
	{
		return _fileChosen;
	}
	
	public int getErrorMessage()
	{
		if ( !mExternalStorageAvailable) {
			return 0;
		} else 
			return 0;
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0 B";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
