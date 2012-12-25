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

package com.putlocker.upload.manager;

import java.util.List;

import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;
import com.putlocker.upload.storage.PutlockerUploadJob;
import com.putlocker.upload.storage.TypedStorageInterface;

public class DownloadsManager {
	TypedStorageInterface<Persistable> _persistantSotrage;
	
	public DownloadsManager(TypedStorageInterface<Persistable> persistantSotrage) {
		// this is going to iniailize onCreate and mark all the current downloads
		// as failed, after a reboot/crash
		_persistantSotrage = persistantSotrage;
	}
	
	public void markAllDownloads() throws InstantiationException, IllegalAccessException
	{
		List<PutlockerDownloadJob> jobs=_persistantSotrage.getTyped(PutlockerDownloadJob.class);
		
		for ( PutlockerDownloadJob job : jobs)
		{
			if ( job.getStatus() == DownloadStatus.JobStarted || job.getStatus() == DownloadStatus.DetailsRequesting ) {
				job.setStatus(DownloadStatus.JobError);
				_persistantSotrage.UpdateTyped(job);
			}
		}
		List<PutlockerUploadJob> uploadJobs =_persistantSotrage.getTyped(PutlockerUploadJob.class);
		
		for ( PutlockerUploadJob job : uploadJobs)
		{
			if ( job.getStatus() == DownloadStatus.JobStarted || job.getStatus() == DownloadStatus.DetailsRequesting ) {
				job.setStatus(DownloadStatus.JobError);
				_persistantSotrage.UpdateTyped(job);
			}
		}
		
	}
}
