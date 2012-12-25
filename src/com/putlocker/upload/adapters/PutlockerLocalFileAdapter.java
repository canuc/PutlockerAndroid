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

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.putlocker.upload.R;
import com.putlocker.upload.storage.PutlockerNode;
import com.putlocker.upload.storage.PutlockerUpDownloadJob;
import com.putlocker.upload.util.FileFactory;

public class PutlockerLocalFileAdapter extends PutlockerFileAdapter<File> {
	public static class RemoteFolderHolder {
		public TextView nameView;
		public ImageView iconView;
		public TextView updateTimeView;
		public TextView	fileSizeView;
	};

	private Activity _ctx;
	private File _filePath;
	
	public PutlockerLocalFileAdapter(Activity context, int textViewResourceId,
			File objects [], File filePath) {
		super(context, textViewResourceId, objects);
		_ctx = context;
		_filePath = filePath;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View currentView = convertView;
		final RemoteFolderHolder holder;
		
		if ( getItemViewType(position) == ITEM_TYPE_HEADER) {
			return getHeaderView(convertView, parent,_filePath.getAbsolutePath());
		}
		
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			currentView = vi.inflate(R.layout.remote_download_item, null);

			holder = new RemoteFolderHolder();
			holder.iconView = (ImageView) currentView.findViewById(R.id.icon_image_view);
			holder.nameView = (TextView) currentView.findViewById(R.id.item_name);
			holder.updateTimeView = (TextView) currentView.findViewById(R.id.item_update_time);
			holder.fileSizeView = (TextView) currentView.findViewById(R.id.item_size);
			currentView.setTag(holder);
		} else {
			holder = (RemoteFolderHolder) currentView.getTag();
		}
		
		
		File currNode = getItem(position);

		if (currNode.isDirectory()) {
			holder.iconView.setImageResource(R.drawable.folder);
			holder.updateTimeView.setVisibility(View.GONE);
			holder.fileSizeView.setVisibility(View.GONE);
		} else {
			holder.iconView.setImageResource(getFileResourceForFilename(currNode.getName()));
			holder.fileSizeView.setVisibility(View.VISIBLE);
			holder.updateTimeView.setVisibility(View.GONE);
			holder.fileSizeView.setText(FileFactory.readableFileSize(currNode.length()));
		}
		
		holder.nameView.setText(currNode.getName());
		
		return currentView;
	}
	
	@Override
	public int getViewTypeCount()
	{
		return 3;
	}
	
	@Override
	public int getItemViewType(int position) {
		if ( position == 0 ) {
			return ITEM_TYPE_HEADER;
		} else {
			return ITEM_TYPE_ITEM; 
		}
	}
	
	@Override
	public File getItem(int position)
	{
		if ( position == 0 ) {
			return null;
		}
		
		int realArrayPosition = position - 1;
		
		return super.getItem(realArrayPosition);
	}
	
	@Override
	public int getCount()
	{
		return  (1  + super.getCount());
	}
}
