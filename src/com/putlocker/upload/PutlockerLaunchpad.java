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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import com.kik.platform.KikClient;
import com.kik.platform.KikData;
import com.kik.platform.KikMessage;
import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.adapters.PutlockerLocalFileAdapter;
import com.putlocker.upload.concurrency.LooperSetUp;
import com.putlocker.upload.http.HttpFetch;
import com.putlocker.upload.http.PutlockerFileHashRequest;
import com.putlocker.upload.http.PutlockerFileLocationRequest;
import com.putlocker.upload.http.RequestCallback;
import com.putlocker.upload.http.UploadHashRequest;
import com.putlocker.upload.http.PutlockerFileHashRequest.PutlockerDownloadRequestType;
import com.putlocker.upload.storage.AuthStorage;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerUploadJob;
import com.putlocker.upload.storage.TypedStorageInterface;
import com.putlocker.upload.util.PutlockerSha1;
import com.tapfortap.TapForTap;

/*
 * This activity is going to query the application and try to retrieve 
 * the required info from storage
 */	
public class PutlockerLaunchpad extends ActivityBase implements RequestCallback {

	public static final String ACTION_DOWNLOAD = "PutlockerLaunchpad.ACTION_DOWNLOAD";
	public static final String ACTION_UPLOAD_FILE = "PutlockerLaunchpad.ACTION_UPLOAD";
	public static final String DOWNLOAD_URL = "PutlockerLaunchpad.DOWNLOAD_URL";
	public static final String ACTION_UPLOAD_FILE_FOLDER = "PutlockerLaunchpad.ACTION_UPLOAD_FILE_FOLDER";
	public static final String FILE_URL = "PutlockerLaunchpad.FILE_URL";

	String uploadFileLocation;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent currIntent = getIntent();
		AuthStorage authStorage = new AuthStorage();
		PutlockerApplication app = (PutlockerApplication) getApplication();
		AuthStorage p = (AuthStorage) app.getStorage().getTyped(authStorage);
		KikData messageData = KikClient.getDataFromIntent(getIntent());
		TapForTap.initialize(this, "2224198dff42f00507fc991866fcfc60");
		
		if (p != null ) {
			try {
				TapForTap.setUserAccountId(PutlockerSha1.SHA1(p.getValueForKey(AuthStorage.AUTH_USERNAME)));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
			}
		}
		if (messageData.getType() == KikData.TYPE_NOT_KIK) {

			if (currIntent.getAction().equals(Intent.ACTION_VIEW)
					|| currIntent.getAction().equals(ACTION_DOWNLOAD)) {
				String dataString = null;
				if (currIntent.hasExtra(DOWNLOAD_URL)) {
					dataString = currIntent.getStringExtra(DOWNLOAD_URL);
				} else {
					dataString = currIntent.getDataString();
				}

				startDownload(dataString);
			} else if (currIntent.getAction().equals(Intent.ACTION_SEND)) {
				String dataString = null;
				String folderHash = null;
				if (currIntent.hasExtra(ACTION_UPLOAD_FILE)) {
					dataString = currIntent.getStringExtra(ACTION_UPLOAD_FILE);
					folderHash = currIntent.getStringExtra(ACTION_UPLOAD_FILE_FOLDER);
				} else {
					Uri data = currIntent.getData();
					if ( data == null ) {
						data = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
						if (  "file".equals(data.getScheme())) {
							File f = new File(data.getPath());
							if ( f.exists() ) {
								dataString = f.getAbsolutePath();
							}
						} else if ( "content".equals(data.getScheme())) {
							dataString = getRealPathFromURI(data, android.provider.MediaStore.Images.ImageColumns.DATA);
						}
					} else {
						File f = new File(data.getPath());
						if ( f.exists() ) {
							dataString = f.getAbsolutePath();
						}
					}
					
					
				}

				if (p == null) {
					Intent newIntent = new Intent(this,
							PutlockerUploadActivity.class);
					startActivity(newIntent);
					finish();
				} else {
					if ( dataString != null ) {
						startUpload(dataString,folderHash);
					} else {
						finish();
					}
				}

			} else {
				if (p != null) {
					Intent newIntent = new Intent(this, PutlockerHome.class);
					startActivity(newIntent);
				} else {
					Intent newIntent = new Intent(this,
							PutlockerUploadActivity.class);
					startActivity(newIntent);
				}
				finish();
			}
		} else {
			if (messageData.getType() == KikData.TYPE_PICK) {
				if (p != null) {
					Intent newIntent = new Intent(this, PutlockerShareItem.class);
					startActivity(newIntent);
				} else {
					Intent newIntent = new Intent(this,
							PutlockerUploadActivity.class);
					startActivity(newIntent);
				}
				finish();
			} else {
				KikMessage msg = messageData.getMessage();
				HashMap<String, String> messageExtras = msg.getExtras();
				String url = messageExtras.get(FILE_URL);
				startDownload(url);
			}
		}
	}
	
	protected void startDownload(String url) {

		setContentView(R.layout.launchpad_activity);
		final PutlockerFileHashRequest fetch = new PutlockerFileHashRequest(
				url, this);
		getPutlockerApplication().getThread().setLooperStartedCallback(
				new LooperSetUp() {

					@Override
					public void LooperStarted() {
						getPutlockerApplication().getThread().mHandler
								.post(fetch);
					}
				});
	}
	

	protected void startUpload(String url,String folder) {
		AuthStorage authStorage = new AuthStorage();
		PutlockerApplication app = (PutlockerApplication) getApplication();
		AuthStorage p = (AuthStorage) app.getStorage().getTyped(authStorage);
		setContentView(R.layout.launchpad_activity);
		TextView tv = (TextView) findViewById(R.id.download_text);
		tv.setText(R.string.begining_upload);
		uploadFileLocation = url;

		final UploadHashRequest req = new UploadHashRequest(p, this,folder);
		getPutlockerApplication().getThread().setLooperStartedCallback(
				new LooperSetUp() {

					@Override
					public void LooperStarted() {
						getPutlockerApplication().getThread().mHandler
								.post(req);
					}
				});
	}

	@Override
	public void requestSuccess(HttpFetch fetch) {
		PutlockerApplication app = (PutlockerApplication) getApplication();
		TypedStorageInterface<Persistable> storage = app.getStorage();
		if (fetch instanceof PutlockerFileHashRequest) {
			PutlockerFileHashRequest file = (PutlockerFileHashRequest) fetch;
			if ( file.getType() == PutlockerDownloadRequestType.FileHash) {
			PutlockerFileLocationRequest location = new PutlockerFileLocationRequest(
					file.getRequestedUrl(), this, file, file.getHashToken());
			getPutlockerApplication().getThread().mHandler.post(location);
			} else if (file.getType() ==PutlockerDownloadRequestType.FileLocation ) {
				operateOnLocation(file);
			}
		} else if (fetch instanceof PutlockerFileLocationRequest) {
			final PutlockerFileLocationRequest req = (PutlockerFileLocationRequest) fetch;
			operateOnLocation(req);
		} else if (fetch instanceof UploadHashRequest) {
			final UploadHashRequest hashReq = (UploadHashRequest) fetch;

			final PutlockerUploadJob upload = new PutlockerUploadJob();
			upload.setFileLocation(uploadFileLocation);
			File f = new File(uploadFileLocation);
			upload.setName(f.getName());
			upload._uploadHash = hashReq.getUploadHash();
			upload.setTotalFileSize(f.length());
			upload._sessionId = hashReq.getCookie("PHPSESSID");
			upload._folderId = hashReq.getFolderHash();
			
			storage.storeTyped("someKey", upload);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(PutlockerLaunchpad.this,
							DownloadService.class);
					intent.putExtra(DownloadService.JOB_EXTRA_UPLOAD, upload);
					startService(intent);
					
					Intent uploadIntent = new Intent(PutlockerLaunchpad.this,PutlockerTransferList.class);
					startActivity(uploadIntent);
					finish();
				}
			});
		}
	}
	
	protected void operateOnLocation(final PutlockerFileLocationRequest req)
	{
		PutlockerApplication app = (PutlockerApplication) getApplication();
		TypedStorageInterface<Persistable> storage = app.getStorage();
		storage.storeTyped("someKey", req.getJobLocation());
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				/*
				 * Process the putlocker file location, now we have a link
				 * and all the required cookies
				 */
				Intent intent = new Intent(PutlockerLaunchpad.this,
						DownloadService.class);
				intent.putExtra(DownloadService.JOB_EXTRA_DOWNLOAD,
						req.getJobLocation());
				startService(intent);
				
				Intent uploadIntent = new Intent(PutlockerLaunchpad.this,PutlockerTransferList.class);
				startActivity(uploadIntent);
				
				finish();
			}
		});
	}

	@Override
	public void requestFailure(HttpFetch fetch) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog diag;
				diag = new AlertDialog.Builder(PutlockerLaunchpad.this)
						.setTitle(R.string.error_title)
						.setMessage(R.string.error_downloading)
						.setCancelable(false)
						.setPositiveButton(R.string.okay,
								new OnClickListener() {

									@SuppressWarnings("null")
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										PutlockerLaunchpad.this.finish();
									}
								}).create();
				diag.show();
			}
		});
	}

	@Override
	public void requestRetry(HttpFetch fetch) {

	}

}
