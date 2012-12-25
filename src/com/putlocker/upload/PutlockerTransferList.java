package com.putlocker.upload;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.adapters.DownloadArrayAdapter;
import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.concurrency.interfaces.PutlockerTransferResponder;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerUpDownloadJob;
import com.putlocker.upload.storage.PutlockerUpDownloadJob.DownloadStatus;
import com.putlocker.upload.storage.PutlockerUploadJob;
import com.putlocker.upload.storage.TypedStorageInterface;
import com.putlocker.upload.util.FileFactory;
import com.putlocker.upload.util.MimeTypeMap;

public class PutlockerTransferList extends ActivityBase implements PutlockerTransferResponder, OnItemClickListener {
	private ListView _list;
	private DownloadArrayAdapter _adapter;
	private static int DOWNLOAD_STOP_DOWNLOAD = 0;
	private static int DOWNLOAD_DELETE_DOWNLOAD = 1;

	public static final String SCROLL_TO_TRANSFER = "PutlockerTransferList.SCROLL_TO_TRANSFER";
	
	private PutlockerUpDownloadJob _menuItemSelected;
	
	public PutlockerTransferList() {
		// Now we will create a list of all the downloads that we have available to us
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * This will set the bundles saved state
		 */
		setContentView(R.layout.download_list);
		//_adView = (AdView) findViewById(R.id.ad_view);
		_list = (ListView) findViewById(R.id.list);
		_list.setDividerHeight(0);
		
//		_adView.setListener(new AdViewListener() {
//		    public void onReceiveAd() {
//		        Log.d("MyActivity", "Tap for Tap ad received");
//		    }
//
//		    public void onFailToReceiveAd(String reason) {
//		        Log.d("MyActivity", "Tap for Tap failed to receive ad: " + reason);
//		    }
//
//		    public void onTapAd() {
//		        Log.d("MyActivity", "Tap for Tap ad tapped");
//		    }
//		});
		doAddItems();
		
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		_list.setOnItemClickListener(this);
		registerForContextMenu(_list);
	}
	
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_transfers,(com.actionbarsherlock.view.Menu) menu);
	    return true;
	}
	

	protected void clearAll()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Delete All Uploads/Downloads?")
		.setMessage("Are you sure you would like to clear this list of all transfers?")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PutlockerApplication app = (PutlockerApplication) getApplication();
				TypedStorageInterface<Persistable> persistableStorage = app.getStorage();
				
				persistableStorage.deleteWhere(new PutlockerDownloadJob(),getWhereClauseForDownloads());
				persistableStorage.deleteWhere(new PutlockerUploadJob(),getWhereClauseForUploads());
				doAddItems();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).create().show();
	}
	
	protected static String getWhereClauseForDownloads()
	{
		
		return PutlockerDownloadJob.DOWNLOAD_STATUS_KEY+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobSucess.getValue() + "\" OR " +
				PutlockerDownloadJob.DOWNLOAD_STATUS_KEY+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobError.getValue() + "\" OR " +
				PutlockerDownloadJob.DOWNLOAD_STATUS_KEY+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobCancelled.getValue() + "\"";
	}
	
	protected static String getWhereClauseForUploads()
	{
		
		return PutlockerUploadJob.PUTLOCKER_UPLOAD_STATUS+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobSucess.getValue() + "\" OR " +
				PutlockerUploadJob.PUTLOCKER_UPLOAD_STATUS+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobError.getValue() + "\" OR " +
				PutlockerUploadJob.PUTLOCKER_UPLOAD_STATUS+"=\""+PutlockerUpDownloadJob.DownloadStatus.JobCancelled.getValue() + "\"";
	}
		
	
	protected void doAddItems()
	{
		
		List<PutlockerDownloadJob> jobs = null;
		List<PutlockerUploadJob> uploadJobs = null;
		PutlockerApplication app = (PutlockerApplication) getApplication();
		TypedStorageInterface<Persistable> persistableStorage = app.getStorage();
		try {
			jobs = persistableStorage.getTyped(PutlockerDownloadJob.class);
			uploadJobs = persistableStorage.getTyped(PutlockerUploadJob.class);
			PutlockerUpDownloadJob [] jobList = new PutlockerUpDownloadJob[jobs.size()];
			PutlockerUpDownloadJob [] uploadJobArray = new PutlockerUpDownloadJob[uploadJobs.size()];
			int i = 0;
			for ( PutlockerDownloadJob job : jobs) {
				// ANR could possibly occur here maby a guard?
				File f = new File(job.getFileLocation());
				// does the file exist?
				if ( f.exists() ) {
					job.downloadedFileSize = f.length();
				}
				
				jobList[i] = job;
				i++;
			}
			
			i = 0;
			for ( PutlockerUploadJob job : uploadJobs) {
				uploadJobArray[i] = job;
				i++;
			}
			
			_adapter = new DownloadArrayAdapter(this, R.layout.list_entry_file,jobList,uploadJobArray);
			_list.setAdapter(_adapter);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	  if (v.getId()==R.id.list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    PutlockerUpDownloadJob job = _adapter.getItem(info.position);
	    DownloadStatus st = job.getStatus();
	    menu.setHeaderTitle(job.getName());
	    menu.add(0,DOWNLOAD_DELETE_DOWNLOAD,0,R.string.context_menu_delete);
	    if ( st.getValue().equals(DownloadStatus.JobStarted.getValue())) {
	    	menu.add(0,DOWNLOAD_STOP_DOWNLOAD,0,R.string.context_menu_stop);
	    }
	    
	    _menuItemSelected = job;
	  }
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  if ( menuItemIndex == DOWNLOAD_DELETE_DOWNLOAD ) {
		  // We are going to take the first element 
		 if (_menuItemSelected.getJobType()==PutlockerUpDownloadJob.DOWNLOAD_JOB) 
		 {
		  final File f = new File(((PutlockerDownloadJob)_menuItemSelected).getFileLocation());
			// does the file exist?
		  if ( f.exists() ) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.delete_file);
				builder.setMessage(R.string.would_you_like_to_delete_file);
				builder.setPositiveButton(R.string.yes, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						f.delete();
						getPutlockerApplication().getStorage().deleteTyped(_menuItemSelected);
						doAddItems();
						 _menuItemSelected = null;
					}
				});
				builder.setNegativeButton(R.string.no, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getPutlockerApplication().getStorage().deleteTyped(_menuItemSelected);
						doAddItems();
						 _menuItemSelected = null;
					}
				});
				builder.setCancelable(false).create().show();
			} else {
				 getPutlockerApplication().getStorage().deleteTyped(_menuItemSelected);
				 doAddItems();
				 _menuItemSelected = null;
			}
		 }else {
			 getPutlockerApplication().getStorage().deleteTyped(_menuItemSelected);
			 doAddItems();
			 _menuItemSelected = null;
		}
	  } else if ( menuItemIndex == DOWNLOAD_STOP_DOWNLOAD ) {
		  DownloadService service = getPutlockerApplication().getService();
		  if ( service != null ) {
			  service.cancelJob(_menuItemSelected);
		  }
		  _menuItemSelected = null;
	  }
	 
	  return true;
	}
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) 
	{
	    if (item.getItemId() == android.R.id.home) 
	    {
	    	finish();
	    	return true;
	    } else if (item.getItemId() == R.id.menu_clear_all )
	    {
	    	clearAll();
	    	return true;
	    }
	    return false;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		DownloadService service = getService();
		if ( service != null ) {
        		service.setNextResponder(this);
        }
		doAddItems();
	}
	
	@Override 
	public void onPause()
	{
		super.onPause();
		DownloadService service = getService();
		if ( service != null ) {
    		service.setNextResponder(null);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void setPutlockerJobStatus(final PutlockerUpDownloadJob job,
			final DownloadStatus status) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// We are going to update the array list status
				PutlockerUpDownloadJob listJob =_adapter.getListJob(job);
				if ( listJob != null ) {
					listJob.setStatus(status);
					_adapter.notifyDataSetChanged();
				}
			}
		});
		
	}

	@Override
	public void setDownloadJobProgress(PutlockerUpDownloadJob job, Long bytes,
			Long bytesTotal) {
		PutlockerUpDownloadJob listJob =_adapter.getListJob(job);
		
		if ( listJob != null ) {
			if ( listJob instanceof PutlockerUploadJob ) {
				((PutlockerUploadJob) job).setTotalUploaded(bytes);
			} else {
				((PutlockerDownloadJob) job).downloadedFileSize = bytes;
			}
		}
		_adapter.setProgressOfJob(job, job.getProgressForDownload());
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		final PutlockerUpDownloadJob job = _adapter.getItem(position);
		if ( job instanceof PutlockerDownloadJob ) {
			if ( isVideo(job.getName())) {
				if (!hasMxPlayerInstalled() && !hasToldNotToPrompt()) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.mx_player_dialog,null);
					final CheckBox dontWarn = (CheckBox) layout.findViewById(R.id.dont_ask_again);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setView(layout);
					builder.setPositiveButton(R.string.take_to_mx_player, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String appName = "com.mxtech.videoplayer.ad";
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appName)));
							if (dontWarn.isChecked())
							{
								setDontWarnAgain();
							}
						}
					}).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (dontWarn.isChecked())
							{
								setDontWarnAgain();
								startJob((PutlockerDownloadJob)job);
							}
						}
					});
					
					builder.create();
					builder.show();
				} else  {
					startJob((PutlockerDownloadJob)job);
				}
			} else {
				startJob((PutlockerDownloadJob)job);
			}
		
		}
	}
	
	protected void setDontWarnAgain()
	{
		SharedPreferences prefs= getPutlockerApplication().getKeyValue();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(PutlockerApplication.MX_WARN, 1);
		editor.commit();
	}
	
	protected void startJob(PutlockerDownloadJob job)
	{
		File f = new File(job.getFileLocation());
		Intent openIntent = new Intent();
		openIntent.setAction(android.content.Intent.ACTION_VIEW);
		openIntent.setDataAndType(Uri.fromFile(f), getMimeTypeForFilename(job._fileName));
		startActivity(Intent.createChooser(openIntent, "Open with:"));
	}
	
	public String getMimeTypeForFilename(String name)
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
	
	public boolean isVideo(String name)
	{
		if ( name != null && name.length() >= 3) {
			int index = name.lastIndexOf('.');
			if ( index != -1 ) {
				String extension = name.substring(index+1);
				return FileFactory.typeFromFileName(extension) == FileFactory.FileType.FILE_TYPE_VIDEO;
			}
		}
		return false;
	}
	
	public boolean hasToldNotToPrompt()
	{
		SharedPreferences prefs= getPutlockerApplication().getKeyValue();
		int warn = prefs.getInt(PutlockerApplication.MX_WARN, 0);
		return (warn == 1);
	}
	
	public boolean hasMxPlayerInstalled()
	{
		return hasAppInstalled("com.mxtech.videoplayer.ad") || hasAppInstalled("com.mxtech.videoplayer.pro");
	}
	
	public boolean hasAppInstalled(String appId)
	{
		boolean hasPackage = true;
		PackageManager pm = getPackageManager();
		try{
		pm.getPackageInfo(appId, 0);
		} catch (NameNotFoundException notFound)
		{
			hasPackage = false;
		}
		return hasPackage;
	}
}
