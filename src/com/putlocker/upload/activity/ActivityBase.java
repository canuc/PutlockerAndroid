/**
 * Putlocker Android - Putlocker scraper for Android 
 *
 * Author: Julian Haldenby (j.haldenby@gmail.com)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  QUbuntuOne is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General
 *  Public License along with Putlocker Android.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.putlocker.upload.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.actionbarsherlock.app.SherlockActivity;
import com.putlocker.upload.DownloadService;
import com.putlocker.upload.PutlockerApplication;
import com.putlocker.upload.R;

public class ActivityBase extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.automotive_common_app_bkg_top_src);
		bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		getSupportActionBar().setBackgroundDrawable(bg);	
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		PutlockerApplication application = getPutlockerApplication();
		application.setCurrentActivity(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PutlockerApplication application = getPutlockerApplication();
		application.unsetCurrentActivity(this);
	}
	
	// And to convert the image URI to the direct file system path of the image file
	protected String getRealPathFromURI(Uri contentUri,String columnName) {
	        String [] proj={MediaStore.Images.Media.DATA};
	        Cursor cursor = managedQuery( contentUri,
	                        proj, // Which columns to return
	                        null,       // WHERE clause; which rows to return (all rows)
	                        null,       // WHERE clause selection arguments (none)
	                        null); // Order-by clause (ascending by name)
	        if ( cursor != null ) {
		        int column_index = cursor.getColumnIndex(columnName);
		        if ( cursor.moveToFirst() ) {
		        	return cursor.getString(column_index);
		        }
	        }
	        return null; 
	}
	
	
	protected final PutlockerApplication getPutlockerApplication()
	{
		return (PutlockerApplication) getApplication();
	}
	
	protected final DownloadService getService()
	{
		PutlockerApplication application = getPutlockerApplication();
		return application.getService();
	}
	
	public boolean isResponder()
	{
		return false;
	}
	
	public String mimeTypeFromFileName(String filename)
	{
		filename = filename.toLowerCase();
		if (filename.equals("apk")) {
			return "application/vnd.android.package-archive";
		} else if ( filename.equals("txt") ) {
			return "text/plain";
		} else if ( filename.equals("csv") ) {
			return "text/csv";
		} else if ( filename.equals("xml") ) {
			return "text/xml";
		} else if ( filename.equals("htm") ) {
			return "text/html";
		} else if ( filename.equals("html") ) {
			return "text/html";
		} else if ( filename.equals("php") ) {
			return "text/php";
		} else if ( filename.equals("png") ) {
			return "image/png";
		} else if ( filename.equals("gif") ) {
			return "image/gif";
		} else if ( filename.equals("jpg") ) {
			return "image/jpg";
		} else if ( filename.equals("jpeg") ) {
			return "image/jpeg";
		} else if ( filename.equals("bmp") ) {
			return "image/bmp";
		} else if ( filename.equals("mp3") ) {
			return "audio/mp3";
		} else if ( filename.equals("wav") ) {
			return "audio/wav";
		} else if ( filename.equals("ogg") ) {
			return "audio/x-ogg";
		} else if ( filename.equals("mid") ) {
			return "audio/mid";
		} else if ( filename.equals("midi") ) {
			return "audio/midi";
		} else if ( filename.equals("amr") ) {
			return "audio/AMR";
		} else if ( filename.equals("mpg") || filename.equals("mpeg") ) {
			return "video/mpeg";
		} else if ( filename.equals("3gp") ) {
			return "video/3gpp";
		} else if ( filename.equals("flv") ) {
			return "video/*";
		} else if ( filename.equals("zip") ) {
			return "application/zip";
		} else if ( filename.equals("jar") ) {
			return "application/java-archive";
		} else if ( filename.equals("zip") ) {
			return "application/zip";
		} else if ( filename.equals("rar") ) {
			return "application/x-rar-compressed";
		} else if ( filename.equals("gz") ) {
			return "application/gzip";
		} else if ( filename.equals("ogg") ) {
			return "audio/x-ogg";
		} else if (filename.equals("avi")) {
			return "video/*";
		} else if (filename.equals("mkv")) {
			return "video/*";
		} else if (filename.equals("mp4")) {
			return "video/*";
		} else if (filename.equals("f4v")) {
			return "video/*";
		} else if (filename.equals("crdownload")) {
			return "video/*";
		} 		
		return "text/*";
	}
	
	protected void showErrorDialog(int errrorRid)
    {
		showErrorDialog(getString(errrorRid));
    }
	
	protected void showErrorDialog(String errorString)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_title);
		builder.setMessage(errorString);
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.create().show();
	}
}

