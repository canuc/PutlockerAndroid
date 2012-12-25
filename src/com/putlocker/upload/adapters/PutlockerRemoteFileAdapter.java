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

import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kik.platform.KikClient;
import com.kik.platform.KikMessage;
import com.putlocker.upload.R;
import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.storage.PutlockerNode;
import com.putlocker.upload.storage.PutlockerNode.NodeType;

public class PutlockerRemoteFileAdapter extends
		PutlockerFileAdapter<PutlockerNode> {

	public static class RemoteFolderHolder {
		public TextView nameView;
		public ImageView iconView;
		public TextView updateTimeView;
		public TextView	fileSizeView;
	};

	private Activity _ctx;

	public PutlockerRemoteFileAdapter(Activity context, int textViewResourceId,
			List<PutlockerNode> objects) {
		super(context, textViewResourceId, objects);
		_ctx = context;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View currentView = convertView;
		final RemoteFolderHolder holder;

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
		
		
		PutlockerNode currNode = getItem(position);

		if (currNode.getNodeType().getTypeName().equals(NodeType.NODE_TYPE_FILE.getTypeName())) {
			holder.iconView.setImageResource(getFileResourceForFilename(currNode.getNodeName()));
			holder.updateTimeView.setVisibility(View.VISIBLE);
			holder.fileSizeView.setVisibility(View.VISIBLE);
			holder.updateTimeView.setText(currNode.getValueForKey(PutlockerNode.NODE_KEY_UPLOAD_DATE));
			holder.fileSizeView.setText(currNode.getValueForKey(PutlockerNode.NODE_KEY_FILE_SIZE));
		} else {
			holder.iconView.setImageResource(R.drawable.folder);
			holder.updateTimeView.setVisibility(View.GONE);
			holder.fileSizeView.setVisibility(View.GONE);
		}
		
		
		holder.nameView.setText(currNode.getNodeName());
		
		return currentView;
	}

}
