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
import java.io.FilenameFilter;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.adapters.PutlockerLocalFileAdapter;

public class FileSelectActivity extends ActivityBase implements OnItemClickListener {
	
	public static final String FILE_LOCATION = "FileSelectActivity.FILE_LOCATION";
	public static final String FILE_UPLOAD_LOCATION = "FileSelectActivity.FILE_UPLOAD_LOCATION";
	
	private File currFile;
	private boolean isInSearchMode = false;
	private String folderHash;
	private Stack<File> _fileStack = new Stack<File>();
	PutlockerLocalFileAdapter _adapter;
	private EditText search;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_file_list);
		
		Intent i = getIntent();
		String fileLocation = i.getStringExtra(FILE_LOCATION);
		if ( i.hasExtra(FILE_UPLOAD_LOCATION)) {
			folderHash = i.getStringExtra(FILE_UPLOAD_LOCATION);
		}
		File f = new File(fileLocation);
		if ( f.exists() && f.isDirectory() ) {
			setListView(f,null);
			_fileStack.add(f);
		} else {
			Toast.makeText(getApplicationContext(), "Cannot open that folder", Toast.LENGTH_LONG).show();
			finish();
		}
		ActionBar bar = getSupportActionBar();
		bar.setTitle("Your Files - Local");
	}
	
	protected void setListView(File file,final String filterString)
	{
		File[] fileList = null;
		if (filterString != null ) {
			FilenameFilter filter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					return filename.toLowerCase().contains(filterString.toLowerCase());
				}
			};
			fileList = file.listFiles(filter);
			isInSearchMode = true;
		} else {
			isInSearchMode = false;
			fileList =  file.listFiles();
		}
		
		ListView list = (ListView) findViewById(R.id.list_remote);
		_adapter = new PutlockerLocalFileAdapter(this, R.layout.local_file_list, fileList, file);
		
	   list.setDividerHeight(0);
	   list.setAdapter(_adapter);
	   list.setOnItemClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
	    menu.add(0, 1, 1, R.string.menu_search).setIcon(R.drawable.action_search).setActionView(R.layout.action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	    return super.onCreateOptionsMenu(menu);
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {
	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	    	if ( s.length() == 0 ) {
	    		setListView(_fileStack.peek(), null);
	    	} else {
	    		setListView(_fileStack.peek(), s.toString());
	    	}
	    }

	};
	
	// called whenever an item in your options menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case 1:
				search = (EditText) item.getActionView().findViewById(R.id.search_bar);
                search.addTextChangedListener(filterTextWatcher);
			break;
		}
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if ( _adapter.getItemViewType(position) == PutlockerLocalFileAdapter.ITEM_TYPE_ITEM )
		{
			File f = _adapter.getItem(position);
			
			if ( f.isDirectory() ) {
				setListView(f,null);
				_fileStack.add(f);
			} else {
				Intent newIntent = new Intent(this,PutlockerLaunchpad.class);
				newIntent.setAction(Intent.ACTION_SEND);
				newIntent.putExtra(PutlockerLaunchpad.ACTION_UPLOAD_FILE, f.getAbsolutePath());
				if ( folderHash != null) {
					newIntent.putExtra(PutlockerLaunchpad.ACTION_UPLOAD_FILE_FOLDER, folderHash);
				}
				startActivity(newIntent);
				finish();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
	   
	    if ( isInSearchMode ) {
	    	setListView(_fileStack.peek(),null);
	    } else {
	    	File f = _fileStack.pop();
		    if (_fileStack.empty()) {
		    	finish();
		    } else {
		    	setListView(_fileStack.peek(),null);
		    }
	    }
	}

}
