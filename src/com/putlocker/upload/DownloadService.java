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

import java.io.File;
import java.util.Hashtable;
import java.util.Random;

import android.support.v4.app.NotificationCompat;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.concurrency.interfaces.PutlockerTransferResponder;
import com.putlocker.upload.concurrency.interfaces.StopableRequest;
import com.putlocker.upload.http.HttpFetch;
import com.putlocker.upload.http.HttpFetch.RequestStatus;
import com.putlocker.upload.http.PutlockerFileDownload;
import com.putlocker.upload.http.PutlockerUploadFile;
import com.putlocker.upload.http.RequestCallback;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerUpDownloadJob;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;
import com.putlocker.upload.storage.PutlockerUploadJob;
import com.putlocker.upload.storage.TypedStorageInterface;
import com.putlocker.upload.util.FileFactory;
import com.putlocker.upload.util.MimeTypeMap;
import com.putlocker.upload.util.FileFactory.FileType;

/**
 * This will manage the download queue. This is nicely handled by the
 * Android's intent service.
 * 
 * 
 * @class DownloadService A service that will be started as a foreground service  
 *
 */
public class DownloadService extends IntentService implements
		PutlockerTransferResponder {
	public static final String JOB_EXTRA_DOWNLOAD = "extras.job.download";
	public static final String JOB_EXTRA_UPLOAD = "extras.job.upload";
	private File baseFile;
	private PutlockerTransferResponder _nextResponder;
	private Object _threadLock = new Object();
	private Handler _mainThread;
	private long _lastProgress = 0l;
	private SparseArray<Notification> downloadJobs = new SparseArray<Notification>();
	private SparseArray<Boolean> cancelledJobs = new SparseArray<Boolean>();
	private SparseArray<StopableRequest> runningJobs = new SparseArray<StopableRequest>();
	
	public DownloadService() {
		super("Download Service");
	}

	/** For showing and hiding our notification. */
	NotificationManager mNM;
	Hashtable<Long, PutlockerUpDownloadJob> _download = new Hashtable<Long, PutlockerUpDownloadJob>();

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		_mainThread = new Handler(getMainLooper());
		
		PutlockerApplication app = (PutlockerApplication) getApplication();
		
		// register the service as started in the application
		app.setService(this);
		
		// This will retrieve the current base acitivity
		ActivityBase base = app.getCurrentActivityBase();

		// Do we have a current activity?
		if (base != null) {
			
			// Is the base activity a responder in our download queue?
			if (base.isResponder()) {
				_nextResponder = (PutlockerTransferResponder) base;
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PutlockerUpDownloadJob upDownJob = null;
		Notification notification = null;

		if (intent.hasExtra(JOB_EXTRA_DOWNLOAD)) {
			final PutlockerDownloadJob job = intent
					.getParcelableExtra(JOB_EXTRA_DOWNLOAD);
			notification = createDownloadNotification(job, this, 0);
			upDownJob = job;
		} else {
			final PutlockerUploadJob job = intent
					.getParcelableExtra(JOB_EXTRA_UPLOAD);
			notification = createDownloadNotification(job, this, 0);
			upDownJob = job;
		}

		if (downloadJobs.size() == 0) {
			startForeground((int) upDownJob.getGlobalId(), notification);
		} else {
			mNM.notify(upDownJob.getGlobalId(), notification);
		}

		downloadJobs.append(upDownJob.getGlobalId(), notification);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		
		// close the notification that we have stopped
		PutlockerApplication app = (PutlockerApplication) getApplication();
		
		// Null out our service
		app.setService(null);
	}

	/**
	 * @param job The job to notify about
	 * @param context The context to use
	 * Show a notification while this service is running.
	 */
	public Notification createDownloadNotification(
			PutlockerUpDownloadJob job, Context context, int progress) {
		CharSequence text;
		if (job.getJobType() == PutlockerUpDownloadJob.DOWNLOAD_JOB ) {
			text = context.getText(R.string.remote_service_started);
		} else {
			text = context.getText(R.string.service_upload_started);
		}
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, PutlockerTransferList.class), 0);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(context
				.getApplicationContext().getPackageName(),
				R.layout.download_progress);
		
		notification.contentView.setImageViewResource(R.id.status_icon,
				R.drawable.ic_launcher);

		if (job.getJobType() == PutlockerUpDownloadJob.DOWNLOAD_JOB) {
			
			if (FileFactory.typeFromFileName(job.getName()) == FileType.FILE_TYPE_VIDEO ) {
				notification.contentIntent = PendingIntent.getActivity(this, 0, getIntentForJob((PutlockerDownloadJob) job),  Intent.FLAG_ACTIVITY_NEW_TASK);
			} else {
				notification.contentIntent = contentIntent;
			}
			
			notification.contentView
					.setTextViewText(
							R.id.status_text,
							context.getString(R.string.download_started,
									job.getName()));
		} else {
			notification.contentView.setTextViewText(R.id.status_text,
					context.getString(R.string.upload_started, job.getName()));
			notification.contentIntent = contentIntent;
		}

		notification.contentView.setProgressBar(R.id.status_progress, 100,
				progress, false);
		return notification;
	}
	
	/**
	 * Create an intent for the job that we are requesting
	 * @param job the job to create the intent for
	 * @return the intent for the mime type
	 */
	Intent getIntentForJob(PutlockerDownloadJob job)
	{
		File f = new File(job.getFileLocation());
		Intent openIntent = new Intent();
		openIntent.setAction(android.content.Intent.ACTION_VIEW);
		openIntent.setDataAndType(Uri.fromFile(f), getMimeTypeForFilename(job._fileName));
		return Intent.createChooser(openIntent, "Open "+job._fileName+" with:");
	}
		
	/**
	 * This attempt to return the mime type from the filename.
	 * @param name the filename string
	 * @return the string mime type
	 */
	String getMimeTypeForFilename(String name)
	{
		if ( name != null && name.length() >= 3) {
			int index = name.lastIndexOf('.');
			if ( index != -1 ) {
				String extension = name.substring(index+1);
				String mimeType =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				return mimeType;
			}
		}
		return "video/*";
	}
	
	void doCompleteNotification(PutlockerUpDownloadJob job, DownloadStatus status)
	{
		CharSequence text;
		CharSequence contentText;
		PendingIntent intent;
		if (job.getJobType() == PutlockerUpDownloadJob.DOWNLOAD_JOB ) {
			if ( status == DownloadStatus.JobSucess ) {
				text = getText(R.string.download_completed_success);
				contentText = getText(R.string.click_to_open);
				intent = PendingIntent.getActivity(this, 0, getIntentForJob((PutlockerDownloadJob) job),  Intent.FLAG_ACTIVITY_NEW_TASK);
			} else {
				text = getText(R.string.download_completed_error);
				contentText = getText(R.string.click_to_see_transfers);
				intent = PendingIntent.getActivity(this, 0,new Intent(this,PutlockerTransferList.class),  Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		} else {
			if ( status == DownloadStatus.JobSucess ) {
				text = getText(R.string.upload_completed_success);
				contentText = getText(R.string.click_to_see_files);
				intent = PendingIntent.getActivity(this, 0,new Intent(this,PutlockerHome.class),  Intent.FLAG_ACTIVITY_NEW_TASK);
			} else {
				text = getText(R.string.upload_completed_error);
				contentText = getText(R.string.click_to_see_transfers);
				intent = PendingIntent.getActivity(this, 0,new Intent(this,PutlockerTransferList.class),  Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		}
		NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
		notif.setContentTitle(text);
		notif.setContentText(contentText);
		notif.setOngoing(false);
		notif.setSmallIcon(R.drawable.ic_launcher);
		notif.setContentIntent(intent);
		notif.setAutoCancel(true);
		mNM.notify(new Random().nextInt(), notif.build());
	}

	protected void updateContentView(PutlockerUpDownloadJob job, int progress) {
		Notification notification = downloadJobs.get(job.getGlobalId());
		if (notification != null) {
			if (_lastProgress != progress) {
				_lastProgress = progress;
				notification.contentView.setProgressBar(R.id.status_progress,
						100, progress, false);
				mNM.notify(job.getGlobalId(), notification);
			}
		}
	}

	@Override
	public void setPutlockerJobStatus(PutlockerUpDownloadJob job,
			PutlockerDownloadJob.DownloadStatus status) {
		PutlockerTransferResponder responder = null;

		synchronized (_threadLock) {
			if (_nextResponder != null) {
				responder = _nextResponder;
			}
		}

		if (responder != null) {
			responder.setPutlockerJobStatus(job, status);
		}
		job.setStatus(status);
		getPutlockerApplication().getStorage().UpdateTyped(job);
	}

	@Override
	public void setDownloadJobProgress(final PutlockerUpDownloadJob job,
			final Long bytes, final Long bytesTotal) {
		final PutlockerTransferResponder responder;

		synchronized (_threadLock) {
			if (_nextResponder != null) {
				responder = _nextResponder;
			} else {
				responder = null;
			}
		}

		_mainThread.post(new Runnable() {
			@Override
			public void run() {
				
				getPutlockerApplication().getStorage().UpdateTyped(job);

				updateContentView(job, job.getProgressForDownload(bytes));
				/**
				 * Now take the responder and get the progress
				 */
				if (responder != null) {
					responder.setDownloadJobProgress(job, bytes, bytesTotal);
				}
			}
		});

	}

	public void setNextResponder(PutlockerTransferResponder responder) {
		synchronized (_threadLock) {
			_nextResponder = responder;
		}
	}
	
	public boolean isDownloading(PutlockerUpDownloadJob job ) 
	{
		return _download.containsKey(job.getGlobalId());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, "Download Wakelock");
		wl.acquire();

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		WifiLock wifiLock = null;
		// We only want to aquire the wifi wake lock
		if (mWifi.isConnected()) {
			wifiLock = ((WifiManager) this
					.getSystemService(Context.WIFI_SERVICE)).createWifiLock(
					WifiManager.WIFI_MODE_SCAN_ONLY, "WlanSilencerScanLock");
			wifiLock.acquire();
		}

		if (intent.hasExtra(JOB_EXTRA_DOWNLOAD)) {
			handleDownloadIntent(intent);
		} else {
			handleUploadIntent(intent);
		}

		if (wifiLock != null && wifiLock.isHeld()) {
			wifiLock.release();
		}

		wl.release();
	}
	

	private void handleDownloadIntent(Intent intent) {
		baseFile = new File("/mnt/sdcard/", "putlocker");
		final PutlockerDownloadJob job = intent
				.getParcelableExtra(JOB_EXTRA_DOWNLOAD);
		if (cancelledJobs.get(job.getGlobalId()) != null) {
			cancelledJobs.remove(job.getGlobalId());
		} else {
			_download.put((long) job.getGlobalId(), job);
			if (!baseFile.exists()) {
				if (!baseFile.mkdir()) {
					Toast.makeText(getApplicationContext(),
							"Could not download, must have an SDCard",
							Toast.LENGTH_LONG);
					setPutlockerJobStatus(job, DownloadStatus.JobError);
					return;
				}
			}
			/*
			 * Update our current file location
			 */
			File newFile = new File(baseFile, String.valueOf(job._fileName));

			job.setFileLocation(newFile.getAbsolutePath());

			getPutlockerApplication().getStorage().UpdateTyped(job);
			Log.e("Julian", "Job id " + String.valueOf(job.getId()));
			RequestCallback callback = new RequestCallback() {
				@Override
				public void requestSuccess(HttpFetch fetch) {
					job.setStatus(DownloadStatus.JobSucess);
					setPutlockerJobStatus(job, DownloadStatus.JobSucess);
					_download.remove(job.getGlobalId());
				}

				@Override
				public void requestRetry(HttpFetch fetch) {
				}

				@Override
				public void requestFailure(HttpFetch fetch) {
					job.setStatus(DownloadStatus.JobError);
					_download.remove(job.getGlobalId());
					setPutlockerJobStatus(job, DownloadStatus.JobError);
					_mainThread.post(new Runnable() {

						@Override
						public void run() {

							Toast.makeText(getApplicationContext(),
									R.string.download_failed, Toast.LENGTH_LONG);
						}
					});

				}
			};
			PutlockerFileDownload download = new PutlockerFileDownload(job,
					this, callback, newFile);
			runningJobs.append(job.getGlobalId(), download);
			download.run();
			
			runningJobs.remove(job.getGlobalId());
			if ( download.getStatus().equals(RequestStatus.RequestStatusCanceled) ) {
				job.setStatus(DownloadStatus.JobError);
				setPutlockerJobStatus(job, DownloadStatus.JobError);
			}
		}
		downloadJobs.remove(job.getGlobalId());
		mNM.cancel(job.getGlobalId());
		// At the end we are going to update the status of the job
		getPutlockerApplication().getStorage().UpdateTyped(job);
		// We are going to stop the foreground services

		if (downloadJobs.size() == 0) {
			stopForeground(true);
		}
		doCompleteNotification(job, job.getStatus());
	}

	private void handleUploadIntent(Intent intent) {
		final PutlockerUploadJob job = intent
				.getParcelableExtra(JOB_EXTRA_UPLOAD);
		if (cancelledJobs.get(job.getGlobalId()) != null) {
			cancelledJobs.remove(job.getGlobalId());
		} else {
			_download.put((long) job.getGlobalId(),job);
			RequestCallback callback = new RequestCallback() {

				@Override
				public void requestSuccess(HttpFetch fetch) {
					job.setStatus(DownloadStatus.JobSucess);
					setPutlockerJobStatus(job, DownloadStatus.JobSucess);
					_download.remove(job.getGlobalId());
				}

				@Override
				public void requestRetry(HttpFetch fetch) {
				}

				@Override
				public void requestFailure(HttpFetch fetch) {
					job.setStatus(DownloadStatus.JobError);
					setPutlockerJobStatus(job, DownloadStatus.JobError);
					_download.remove(job.getGlobalId());
					_mainThread.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									R.string.download_failed, Toast.LENGTH_LONG);
						}
					});

				}
			};
			PutlockerUploadFile file = new PutlockerUploadFile(job, callback,
					this);
			
			runningJobs.append(job.getGlobalId(), file);
			file.run();
			runningJobs.remove(job.getGlobalId());
			
			if ( file.getStatus().equals(RequestStatus.RequestStatusCanceled) ) {
				job.setStatus(DownloadStatus.JobError);
				setPutlockerJobStatus(job, DownloadStatus.JobError);
			}
		}
		downloadJobs.remove(job.getGlobalId());
		mNM.cancel(job.getGlobalId());

		// We are going to stop the foreground services
		if (downloadJobs.size() == 0) {
			stopForeground(true);
		}
		doCompleteNotification(job, job.getStatus());
	}

	protected final PutlockerApplication getPutlockerApplication() {
		return (PutlockerApplication) getApplication();
	}
	
	public void cancelJob(PutlockerUpDownloadJob job)
	{
		StopableRequest request = runningJobs.get(job.getGlobalId());
		if (  request != null ) {
			request.stopRequest();
		} else {
			cancelledJobs.put(job.getGlobalId(), new Boolean(true));
		}
	}
}
