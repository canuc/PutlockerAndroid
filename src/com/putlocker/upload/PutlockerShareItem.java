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
package com.putlocker.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.kik.platform.KikClient;
import com.kik.platform.KikMessage;
import com.putlocker.upload.adapters.PutlockerRemoteFileAdapter;
import com.putlocker.upload.storage.PutlockerNode;
import com.putlocker.upload.storage.PutlockerNode.NodeType;

public class PutlockerShareItem extends PutlockerHome {

	private PutlockerNode _node;
	@Override
	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle("Select Item to Share");
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) 
	{
	    if (item.getItemId() == android.R.id.home) 
	    {
	    	finish();
	    	return true;
	    }
	    return false;
	}
	
	@Override
	protected void itemSelected(final PutlockerNode job)
	{
		if ( job.getNodeType().getTypeName().equals(NodeType.NODE_TYPE_FILE.getTypeName())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setItems(R.array.share_options, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if ( which == 0 ) {
						shareViaKik(job);
					} else if ( which == 1) {
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						clipboard.setText(job.getNodeDownload());
						Toast.makeText(PutlockerShareItem.this, "Copied", Toast.LENGTH_SHORT);
					} else {
						Intent i = new Intent(Intent.ACTION_SEND );
						i.setType("text/plain");
						i.putExtra(android.content.Intent.EXTRA_TEXT, job.getNodeDownload());
						startActivity(Intent.createChooser(i, "Share via"));
						finish();
					}
				}
			});
			
			builder.create().show();
			
			
		}
	}
		
	protected void shareViaKik(PutlockerNode job)
	{
		KikMessage message = new KikMessage("com.putlocker.upload");
		message.setAndroidDownloadUri("market://details?id=com.putlocker.upload");
		message.setFallbackUri(job.getNodeDownload());
		message.setIphoneDownloadUri(job.getNodeDownload());
		message.setAllowForwarding(true);
		message.putExtra(PutlockerLaunchpad.FILE_URL,job.getNodeDownload() );
		Drawable drawable = getResources().getDrawable(PutlockerRemoteFileAdapter.getFileResourceForFilename(job.getNodeName()));
		message.setImage((BitmapDrawable) drawable);
		message.setTitle(job.getNodeName());
		message.setText(this.getString(R.string.send_putlocker_message));
		KikClient.sendMessage((Activity) this, message);
	}
	
	
}
