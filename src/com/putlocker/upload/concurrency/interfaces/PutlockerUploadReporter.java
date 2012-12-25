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

package com.putlocker.upload.concurrency.interfaces;

import com.putlocker.upload.storage.PutlockerUpDownloadJob;
import com.putlocker.upload.storage.PutlockerUploadJob;

public interface PutlockerUploadReporter {
	void setPutlockerJobStatus(PutlockerUploadJob job,PutlockerUpDownloadJob.DownloadStatus status);
	void setDownloadJobProgress(PutlockerUploadJob job,Long bytes,Long bytesTotal);
}
