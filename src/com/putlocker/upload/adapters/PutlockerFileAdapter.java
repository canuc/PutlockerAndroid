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

package com.putlocker.upload.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.putlocker.upload.R;

public class PutlockerFileAdapter<T> extends ArrayAdapter<T> {

	protected int header_text;
	
	public static final int ITEM_TYPE_HEADER = 0;
	public static final int ITEM_TYPE_ITEM = 1;
	
	class HeaderHolder {
		public TextView textView;
	}
	
	public PutlockerFileAdapter(Context context, int resource, int textViewResourceId, T[] objects)
	{
		super(context,resource,textViewResourceId,objects );
	}
	
	public PutlockerFileAdapter(Context context, int textViewResourceId, List<T> objects)
	{
		super(context,textViewResourceId,objects);
	}
	
	public PutlockerFileAdapter(Context context, int textViewResourceId, T [] objects)
	{
		super(context,textViewResourceId,objects);
	}
	
	public static int getFileResourceForFilename(String name)
	{
		if ( name != null && name.length() >= 3) {
			int index = name.lastIndexOf('.');
			if ( index != -1 ) {
				String extension = name.substring(index+1);
				return getResourceForExtension(extension);
			}
		}
		return R.drawable.blank;
	}
	
	public static int getResourceForExtension(String extension)
	{
		if ( extension.equals("ai")) {
			return R.drawable.ai;
		} else if (extension.equals("avi")) {
			return R.drawable.avi;
		} else if (extension.equals("bmp")) {
			return R.drawable.bmp;
		} else if ( extension.equals("wmv")) {
			return R.drawable.wmv;
		} else if ( extension.equals("css") ) {
			return R.drawable.css;
		} else if (extension.equals("dat") ) {
			return R.drawable.dat;
		} else if (extension.equals("dmg")) {
			return R.drawable.dmg;
		} else if (extension.equals("doc")) {
			return R.drawable.doc;
		} else if (extension.equals("dotx")) {
			return R.drawable.dotx;
		} else if (extension.equals("dwg")) {
			return R.drawable.dwg;
		} else if (extension.equals("dxf")) {
			return R.drawable.dxf;
		} else if (extension.equals("eps")) {
			return R.drawable.eps;
		} else if (extension.equals("exe")) {
			return R.drawable.exe;
		} else if (extension.equals("flv")) {
			return R.drawable.flv;
		} else if (extension.equals("gif")) {
			return R.drawable.gif;
		} else if (extension.equals("h")) {
			return R.drawable.h;
		} else if (extension.equals("hpp")) {
			return R.drawable.hpp;
		} else if (extension.equals("html")) {
			return R.drawable.html;
		} else if (extension.equals("ics")) {
			return R.drawable.ics;
		} else if (extension.equals("iso")) {
			return R.drawable.iso;
		} else if (extension.equals("java")) {
			return R.drawable.java;
		} else if (extension.equals("jpg")) {
			return R.drawable.jpg;
		} else if (extension.equals("key")) {
			return R.drawable.key;
		} else if (extension.equals("mid")) {
			return R.drawable.mid;
		} else if (extension.equals("mp3")) {
			return R.drawable.mp3;
		} else if (extension.equals("mp4")) {
			return R.drawable.mp4;
		} else if (extension.equals("mpg") || extension.equals("mpeg"))  {
			return R.drawable.mpg;
		} else if (extension.equals("odf")) {
			return R.drawable.odf;
		} else if (extension.equals("ods")) {
			return R.drawable.ods;
		} else if (extension.equals("odt")) {
			return R.drawable.odt;
		} else if (extension.equals("odp")) {
			return R.drawable.dxf;
		} else if (extension.equals("ots")) {
			return R.drawable.dxf;
		} else if (extension.equals("ott")) {
			return R.drawable.dxf;
		} else if (extension.equals("pdf")) {
			return R.drawable.dxf;
		} else if (extension.equals("php") ) {
			return R.drawable.dxf;
		} else if (extension.equals("png")) {
			return R.drawable.png;
		} else if (extension.equals("ppt")) {
			return R.drawable.ppt;
		} else if (extension.equals("psd")) {
			return R.drawable.psd;
		} else if (extension.equals("py")) {
			return R.drawable.py;
		} else if (extension.equals("qt")) {
			return R.drawable.qt;
		} else if (extension.equals("rar")) {
			return R.drawable.rar;
		} else if (extension.equals("rb")) {
			return R.drawable.rb;
		} else if (extension.equals("rtf")) {
			return R.drawable.rtf;
		} else if (extension.equals("sql")) {
			return R.drawable.sql;
		} else if (extension.equals("tga")) {
			return R.drawable.tga;
		} else if (extension.equals("tgz") || extension.equals("gz")) {
			return R.drawable.gz;
		} else if (extension.equals("tiff")) {
			return R.drawable.tiff;
		} else if (extension.equals("wav")) {
			return R.drawable.wav;
		} else if (extension.equals("xls")) {
			return R.drawable.xls;
		} else if (extension.equals("xlsx")) {
			return R.drawable.xlsx;
		} else if (extension.equals("xml")) {
			return R.drawable.xml;
		} else if (extension.equals("yml")) {
			return R.drawable.yml;
		} else if (extension.equals("zip")) {
			return R.drawable.zip;
		} else if (extension.equals("mkv")) {
			return R.drawable.mkv;
		} else if (extension.equals("log")) {
			return R.drawable.log;
		} else {
			return R.drawable.blank;
		}
	}
	
	protected View getHeaderView(View convertView, ViewGroup parent, String headerText)
	{
		View currentView = null;
		HeaderHolder holder;
		
		if ( convertView == null ) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			currentView = vi.inflate(R.layout.download_header, null);
			holder = new HeaderHolder();
			holder.textView = (TextView) currentView.findViewById(R.id.header);
			currentView.setTag(holder);
		} else {
			currentView = convertView;
			holder = (HeaderHolder) currentView.getTag();
		}
		holder.textView.setText(headerText);
		return currentView;
	}
	
	
	
}
