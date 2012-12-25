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

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.putlocker.upload.R;
import com.putlocker.upload.storage.PutlockerUpDownloadJob;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;
import com.putlocker.upload.storage.PutlockerUploadJob;

/**
 * @author julian
 *
 */
public class DownloadArrayAdapter extends PutlockerFileAdapter<PutlockerUpDownloadJob> {
	SparseArray<ProgressBar> downloadJobs = new SparseArray<ProgressBar>();
	
	
	SparseArray<PutlockerUpDownloadJob> _jobArray = new SparseArray<PutlockerUpDownloadJob>();
	Activity _ctx;
	int layoutResourceId;
	boolean hasUploads = false;
	int headerIndex = 0;
	class ViewHolder {
		public TextView textView;
		public ImageView iconView;
		public PutlockerUpDownloadJob job;
		public ProgressBar progressBar;
		public TextView statusText;
	}
	
	class HeaderHolder {
		public TextView textView;
	}
	
	public DownloadArrayAdapter(Activity context, int textViewResourceId,PutlockerUpDownloadJob [] jobs,PutlockerUpDownloadJob[] uploads) {
		super(context, textViewResourceId,concat(jobs, uploads));
		_ctx = context;
		for( PutlockerUpDownloadJob job : jobs) {
			_jobArray.append(job.getGlobalId(), job);
		}
		for( PutlockerUpDownloadJob job : uploads) {
			_jobArray.append(job.getGlobalId(), job);
		}
		if ( uploads != null && uploads.length > 0 ) {
			headerIndex = jobs.length;
			hasUploads = true;
		}
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View currentView = convertView;
		final ViewHolder holder;
		if ( getItemViewType(position) == ITEM_TYPE_HEADER) {
			return getHeaderView(convertView, parent, hasUploads && position == headerIndex + 1 ? _ctx.getString(R.string.transfers_uploads) : _ctx.getString(R.string.transfers_downloads) );
		}
		
		if ( convertView == null ) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			currentView = vi.inflate(R.layout.list_entry_file, null);
			holder = new ViewHolder();
			holder.textView = (TextView) currentView.findViewById(R.id.item_name);
			holder.iconView = (ImageView) currentView.findViewById(R.id.icon_image_view);
			holder.progressBar = (ProgressBar) currentView.findViewById(R.id.updown_progress);
			holder.progressBar.setMax(100);
			holder.statusText = (TextView) currentView.findViewById(R.id.item_status);
			currentView.setTag(holder);
			
		} else {
			 holder = (ViewHolder) currentView.getTag();
			 downloadJobs.remove(holder.job.getGlobalId());
		}
		
		holder.job = getItem(position);
		holder.progressBar.setProgress(holder.job.getProgressForDownload());
		holder.statusText.setText(getStatusText(holder.job));
		holder.textView.setText(holder.job.getName());
		holder.iconView.setImageResource(getFileResourceForFilename(holder.job.getName()));
		downloadJobs.append(holder.job.getGlobalId(), holder.progressBar);
		return currentView;
	}
	
	public void setProgressOfJob(PutlockerUpDownloadJob job, int progress) {
		ProgressBar bar = downloadJobs.get(job.getGlobalId());
		
		if ( bar != null ) {
			bar.setProgress(progress);
		}
	}
	
	public String getStatusText(PutlockerUpDownloadJob job)
	{
		PutlockerUpDownloadJob.DownloadStatus status = job.getStatus();
		if ( status.equals(DownloadStatus.JobError)) {
			return "Error";
		} else if ( status.equals(DownloadStatus.JobSucess)) {
			return "Success";
		} else if ( status.equals(DownloadStatus.JobStarted)) {
			if ( job instanceof PutlockerUploadJob ) {
				return "Uploading";
			} else {
				return "Downloading";
			}
		} else if ( status.equals(DownloadStatus.JobCancelled)) {
			return "Canceled";
		} else {
			return "Beginning";
		}
	}
	
	public PutlockerUpDownloadJob getListJob(PutlockerUpDownloadJob job)
	{
		return _jobArray.get(job.getGlobalId());
	}
	@Override
	public int getViewTypeCount()
	{
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		if ( position == 0 ) {
			return ITEM_TYPE_HEADER;
		} else {
			if ( hasUploads ) {
				if (position == headerIndex + 1) {
					return ITEM_TYPE_HEADER;
				} else {
					return ITEM_TYPE_ITEM;
				}
			}
			return ITEM_TYPE_ITEM;
		}
	}
	
	@Override
	public PutlockerUpDownloadJob getItem(int position)
	{
		if ( position == 0 ) {
			return null;
		} 
		if ( hasUploads ) {
			if ( headerIndex >= position ) {
				return super.getItem(position - 1);
			} else if (headerIndex + 1 == position ) {
				return null;
			} else {
				return super.getItem(position -2);
			}
		}
		return super.getItem(position -1);
	}
	
	@Override
	public int getCount()
	{
		if ( hasUploads == true ) {
			return super.getCount() + 2;
		}
		return super.getCount()+1;
	}
	public static PutlockerUpDownloadJob[] concat(PutlockerUpDownloadJob[] first, PutlockerUpDownloadJob[] second) {
		  PutlockerUpDownloadJob[] result = new PutlockerUpDownloadJob[first.length+ second.length];
		  System.arraycopy(first, 0, result, 0, first.length);
		  System.arraycopy(second, 0, result, first.length, second.length);
		  return result;
		}
	
}
