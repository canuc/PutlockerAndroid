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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.adapters.PutlockerRemoteFileAdapter;
import com.putlocker.upload.concurrency.LooperSetUp;
import com.putlocker.upload.concurrency.PutlockerDownloadJob;
import com.putlocker.upload.http.FileListRequest;
import com.putlocker.upload.http.HttpFetch;
import com.putlocker.upload.http.RequestCallback;
import com.putlocker.upload.http.filemanagement.PutlockerDeleteFile;
import com.putlocker.upload.storage.AuthStorage;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PutlockerNode;
import com.putlocker.upload.storage.PutlockerNode.NodeType;
import com.putlocker.upload.storage.PutlockerUploadJob;
import com.putlocker.upload.storage.TypedStorageInterface;
import com.putlocker.upload.util.FileFactory;
import com.putlocker.upload.util.HashMapUtils;

public class PutlockerHome extends ActivityBase implements RequestCallback,
		OnItemClickListener, OnItemSelectedListener {

	public static final int ACTIVITY_ITEM_IMAGE = 1001;
	public static final int ACTIVITY_ITEM_VIDEO = 1002;

	public static final int PREFERENCE_SORTFILE_NAME = 0;
	public static final int PREFERENCE_SORT_EXTENSION = 1;

	public static final String FOLDER_ID = "PutlockerHome.folderID";
	public static final int ITEM_DELETE = 1;
	PutlockerRemoteFileAdapter _adapter;
	HashMap<String, PutlockerNode> _nodes = new HashMap<String, PutlockerNode>();

	SharedPreferences sharedPrefs;
	ImageView shareIcon;
	ImageView downloads;
	ImageView uploadFile;

	TextView titleView;
	ListView _list;
	int folderID;
	PutlockerNode folderNode;
	PutlockerNode _menuItemSelected;

	AlertDialog _deleteFileDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		AuthStorage authStorage = new AuthStorage();
		final PutlockerApplication app = (PutlockerApplication) getApplication();
		List<PutlockerNode> nodes = new LinkedList<PutlockerNode>();
		sharedPrefs = getSharedPreferences("sortOrder", Context.MODE_PRIVATE);
		Intent intent = getIntent();
		final int folderID;

		if (intent != null) {
			folderID = intent.getIntExtra(FOLDER_ID, 0);
		} else {
			folderID = 0;
		}

		if (folderID != 0) {
			folderNode = app.getStorage().getTyped(new PutlockerNode(folderID));
		}

		Persistable p = app.getStorage().getTyped(authStorage);

		try {
			nodes = app.getStorage().getTypedWhere(PutlockerNode.class,
					PutlockerNode.NODE_PARENT_KEY, String.valueOf(folderID));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageView v = (ImageView) findViewById(R.id.begin_upload_files);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onUploadClicked();
			}
		});
		for (PutlockerNode node : nodes) {
			_nodes.put(node.getNodeKey(), node);
		}
		ActionBar bar = getSupportActionBar();

		AuthStorage castAuthStorage = (AuthStorage) p;

		final FileListRequest fetch = new FileListRequest(castAuthStorage,
				this, folderNode);

		app.getThread().setLooperStartedCallback(new LooperSetUp() {

			@Override
			public void LooperStarted() {
				app.getThread().mHandler.post(fetch);
			}
		});
		_list = (ListView) findViewById(R.id.list_remote);
		setListWithNodes();
	}

	public void orderNodes(List<PutlockerNode> node) {
		final int orderNode = sharedPrefs.getInt("sort",
				PREFERENCE_SORTFILE_NAME);
		Collections.sort(node, new Comparator<PutlockerNode>() {
			@Override
			public int compare(PutlockerNode lhs, PutlockerNode rhs) {
				if (lhs.getNodeKey().equals(rhs.getNodeKey())) {
					return 0;
				}
				if (lhs.getNodeType() != rhs.getNodeType()) {
					if (lhs.getNodeType() == NodeType.NODE_TYPE_FOLDER) {
						return -1;
					} else {
						return 1;
					}
				} else {
					if (orderNode == PREFERENCE_SORTFILE_NAME) {
						return lhs.getNodeName().compareToIgnoreCase(
								rhs.getNodeName());
					} else {
						if (FileFactory
								.fileExtensionFromName(lhs.getNodeName())
								.compareToIgnoreCase(
										FileFactory.fileExtensionFromName(rhs
												.getNodeName())) == 0
								&& !rhs.getNodeKey().equals(rhs.getNodeKey())) {
							return lhs.getNodeName().compareToIgnoreCase(
									rhs.getNodeName());
						} else {
							return FileFactory.fileExtensionFromName(
									lhs.getNodeName()).compareToIgnoreCase(
									FileFactory.fileExtensionFromName(rhs
											.getNodeName()));
						}
					}
				}
			}
		});
	}

	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.home_menu,(com.actionbarsherlock.view.Menu) menu);
	    int index = 0;
	    try {
	    while( menu.getItem(index)!= null ) {
	    	MenuItem item = menu.getItem(index);
	    	if ( item.getItemId() == R.id.menu_transfers ) {
	    		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Intent i = new Intent(PutlockerHome.this,PutlockerTransferList.class);
						startActivity(i);;
						return true;
					}
				});
	    	} else if ( item.getItemId() == R.id.menu_upload ) {
	    		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						onUploadClicked();
						return true;
					}
				});
	    	} else if ( item.getItemId() == R.id.menu_more) {
	    		int subMenuIndex = 0;
	    		SubMenu subMenu = item.getSubMenu();
	    		
	    		try {
	    			while (subMenu.getItem(subMenuIndex) != null) {
	    				MenuItem currItem = subMenu.getItem(subMenuIndex);
	    				if ( currItem.getItemId() == R.id.menu_share_item) {
	    					currItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    						
	    						@Override
	    						public boolean onMenuItemClick(MenuItem item) {
	    							Intent i = new Intent(PutlockerHome.this,PutlockerShareItem.class);
	    							startActivity(i);
	    							return true;
	    						}
	    					});
	    			    } else if ( currItem.getItemId() == R.id.menu_log_out ) {
	    					currItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    						
	    						@Override
	    						public boolean onMenuItemClick(MenuItem item) {
	    							logOut();
	    							return true;
	    						}
	    			
	    					});
	    		    	} else if ( currItem.getItemId() == R.id.menu_licenses ) {
	    		    		currItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    						
	    						@Override
	    						public boolean onMenuItemClick(MenuItem item) {
	    							TextView textView = new TextView(PutlockerHome.this);
	    							textView.setText(R.string.licenses);
	    							textView.setAutoLinkMask(Linkify.ALL);
	    							AlertDialog.Builder builder = new AlertDialog.Builder(PutlockerHome.this);
	    							builder.setTitle("Licenses").setView(textView).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											
										}
									});
	    							builder.create().show();
	    							return true;
	    						}
	    					});
	    		    	} else if ( currItem.getItemId() == R.id.menu_sort) {
	    		    		currItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		    		    		@Override
	    						public boolean onMenuItemClick(MenuItem item) {
			    		    		AlertDialog.Builder newBuilder = new AlertDialog.Builder(PutlockerHome.this);
									newBuilder.setTitle(R.string.media_select);
									newBuilder.setItems(R.array.sort_order_arr, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
											case 0:
												sharedPrefs.edit().putInt("sort",PREFERENCE_SORTFILE_NAME).commit();
												setListWithNodes();
												break;
											case 1:
												sharedPrefs.edit().putInt("sort",PREFERENCE_SORT_EXTENSION).commit();
												setListWithNodes();
												break;
											}
										}
									});
									newBuilder.create().show();
									return true;
		    		    		}	
	    		    		});
	    		    	} else if (currItem.getItemId() == R.id.menu_download ) {
	    		    		currItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		    		    		@Override
	    						public boolean onMenuItemClick(MenuItem item) {
		    		    			AlertDialog.Builder builder = new AlertDialog.Builder(PutlockerHome.this);
		    		    			LayoutInflater vi = (LayoutInflater) PutlockerHome.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    		    			View inflaterView = vi.inflate(R.layout.add_new_dialog, null);
		    		    			builder.setView(inflaterView);
		    		    			
		    		    			final EditText text = (EditText) inflaterView.findViewById(R.id.edit_text_add);
		    		    			
		    		    			builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											String textUrl = text.getText().toString();
											if ( textUrl.length() != 0 && (Pattern.matches(Constants.PUTLOCKER_EXPRESSION_URL, textUrl) || Pattern.matches(Constants.SOCKSHARE_EXPRESSION_URL, textUrl) )) 
											{
												Intent intent = new Intent(PutlockerHome.this,
														PutlockerLaunchpad.class);
												intent.setAction(Intent.ACTION_VIEW);
												
												intent.putExtra(PutlockerLaunchpad.DOWNLOAD_URL, textUrl);
												PutlockerHome.this.startActivity(intent);
											} else {
												AlertDialog.Builder builder = new AlertDialog.Builder(PutlockerHome.this);
												builder.setTitle(R.string.error_title);
												builder.setMessage(R.string.invalid_link);
												builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														
													}
												});
												builder.create().show();
											}
										}
									});
		    		    			
		    		    			builder.show();
		    		    			return true;
		    		    		}
	    		    		});
	    		    	}
	    				subMenuIndex++;
	    			}
	    		}catch (IndexOutOfBoundsException e) 
	    	    {
	    	    }
	    		
	    	} 
		    index++;
	    }
	    } catch (IndexOutOfBoundsException e) 
	    {
	    }
	    return super.onCreateOptionsMenu(menu);
	}

	protected void onUploadClicked() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.upload_from_where);
		builder.setItems(R.array.upload_locations,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 1:
							if (!FileFactory.hasExternalWritableStorage()) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										PutlockerHome.this);
								builder.setTitle(R.string.title_external_storage_mounted);
								builder.setMessage(R.string.body_external_storage_mounted);
								builder.setPositiveButton(R.string.okay,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

											}
										});
								builder.create().show();
							} else {
								Intent i = new Intent(PutlockerHome.this,
										FileSelectActivity.class);
								File fileLocation = Environment
										.getExternalStorageDirectory();
								i.putExtra(FileSelectActivity.FILE_LOCATION,
										fileLocation.getAbsolutePath());
								startActivity(i);
							}
							break;
						case 0:
							AlertDialog.Builder newBuilder = new AlertDialog.Builder(
									PutlockerHome.this);
							newBuilder.setTitle(R.string.media_select);
							newBuilder.setItems(R.array.type_select,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 1:
												Intent mediaChooser = new Intent(
														Intent.ACTION_GET_CONTENT);
												mediaChooser.setType("video/*");
												startActivityForResult(
														mediaChooser,
														ACTIVITY_ITEM_VIDEO);
												break;
											case 0:
												Intent imageChooser = new Intent(
														Intent.ACTION_GET_CONTENT);
												imageChooser.setType("image/*");
												startActivityForResult(
														imageChooser,
														ACTIVITY_ITEM_IMAGE);
												break;
											}
										}
									});
							newBuilder.create().show();

							break;
						}
					}
				});
		builder.create().show();
	}

	protected void logOut() {
		TypedStorageInterface<Persistable> typed = getPutlockerApplication()
				.getStorage();
		PutlockerNode node = new PutlockerNode();
		PutlockerUploadJob uploadJob = new PutlockerUploadJob();
		PutlockerDownloadJob downloadJob = new PutlockerDownloadJob();
		AuthStorage auth = new AuthStorage();

		DownloadService service = getPutlockerApplication().getService();
		if (service != null) {
			Intent intent = new Intent(this, DownloadService.class);
			service.stopService(intent);
		}

		typed.deleteTable(node);
		typed.deleteTable(uploadJob);
		typed.deleteTable(downloadJob);
		typed.deleteTable(auth);

		Intent i = new Intent(this, PutlockerUploadActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}

	protected void setListWithNodes() {
		List<PutlockerNode> listOfNodes = new LinkedList<PutlockerNode>();

		Iterator<Map.Entry<String, PutlockerNode>> it = _nodes.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, PutlockerNode> entry = it.next();
			listOfNodes.add(entry.getValue());
		}

		_list.setOnItemClickListener(this);
		registerForContextMenu(_list);
		orderNodes(listOfNodes);
		_adapter = new PutlockerRemoteFileAdapter(this,
				R.layout.remote_download_item, listOfNodes);
		_list.setDividerHeight(0);

		_list.setAdapter(_adapter);
		updateViewWithNodes();
	}

	protected void updateViewWithNodes() {
		View v = (View) findViewById(R.id.no_files);
		ImageView view = (ImageView) findViewById(R.id.begin_upload_files);
		TextView empty_list_view = (TextView) findViewById(R.id.empty_list_view);
		if (_nodes.size() == 0  ) {
			if ( folderNode != null ) {
				empty_list_view.setText(R.string.folder_no_files);
				view.setVisibility(View.INVISIBLE);
			} else {
				empty_list_view.setText(R.string.no_files);
				view.setVisibility(View.VISIBLE);
			}
			_list.setVisibility(View.GONE);
			v.setVisibility(View.VISIBLE);
			
		} else {
			_list.setVisibility(View.VISIBLE);
			v.setVisibility(View.GONE);
		}
	}

	public void onResume() {
		super.onResume();
		doUpdateFileList();

	}

	protected void doUpdateFileList() {
		Persistable p = getPutlockerApplication().getStorage().getTyped(
				new AuthStorage());
		AuthStorage castAuthStorage = (AuthStorage) p;
		final FileListRequest fetch = new FileListRequest(castAuthStorage,
				this, folderNode);

		getPutlockerApplication().getThread().setLooperStartedCallback(
				new LooperSetUp() {

					@Override
					public void LooperStarted() {
						getPutlockerApplication().getThread().mHandler
								.post(fetch);
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_ITEM_IMAGE
				|| requestCode == ACTIVITY_ITEM_VIDEO) {
			if (resultCode == RESULT_OK) {
				Uri currImageURI = data.getData();

				Intent newIntent = new Intent(this, PutlockerLaunchpad.class);
				newIntent.setAction(Intent.ACTION_SEND);
				String realPath = null;
				try {
					File f = new File(currImageURI.getPath());
					if (f.exists()) {
						realPath = f.getAbsolutePath();
					} else {
						realPath = getRealPathFromURI(
								currImageURI,
								requestCode == ACTIVITY_ITEM_IMAGE ? android.provider.MediaStore.Images.ImageColumns.DATA
										: android.provider.MediaStore.Video.VideoColumns.DATA);
						if (realPath == null) {
							// if we get null then we are going to get the

							realPath = f.getAbsolutePath();

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (realPath == null) {
					Toast.makeText(this, "Sorry could not find the file..",
							Toast.LENGTH_LONG).show();
				} else {
					newIntent.putExtra(PutlockerLaunchpad.ACTION_UPLOAD_FILE,
							realPath);
					startActivity(newIntent);
				}
			}
		}
	}

	@Override
	public void requestSuccess(HttpFetch fetch) {
		if (fetch instanceof FileListRequest) {
			FileListRequest req = (FileListRequest) fetch;
			boolean isDirty = false;
			final HashMap<String, PutlockerNode> _putlockerNodes = req
					.getParsedNodes();
			Iterator<Map.Entry<String, PutlockerNode>> it = _putlockerNodes
					.entrySet().iterator();
			TypedStorageInterface<Persistable> persistableStorage = getPutlockerApplication()
					.getStorage();
			final Vector<PutlockerNode> nodesToAdd = new Vector<PutlockerNode>();

			HashMap<String, PutlockerNode> nodesToRemove = HashMapUtils
					.copyHashMap(_nodes);
			while (it.hasNext()) {
				Map.Entry<String, PutlockerNode> entry = it.next();
				if (!_nodes.containsKey(entry.getKey())) {
					persistableStorage.storeTyped("", entry.getValue());
					_nodes.put(entry.getKey(), entry.getValue());
					nodesToAdd.add(entry.getValue());
				} else {
					PutlockerNode node = _nodes.get(entry.getKey());
					if ( node.isDirty() ) {
						PutlockerNode lastNode = entry.getValue();
						lastNode.setId(node.getId());
						persistableStorage.storeTyped("", lastNode);
						_nodes.put(entry.getKey(),lastNode);
						isDirty = true;
					}
				}
				nodesToRemove.remove(entry.getKey());
			}
			final boolean isDirtyFinal = isDirty;
			final Set<Entry<String, PutlockerNode>> entrySet = nodesToRemove
					.entrySet();
			for (Entry<String, PutlockerNode> deleteEntry : entrySet) {
				persistableStorage.deleteTyped(deleteEntry.getValue());
				_nodes.remove(deleteEntry.getKey());
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateViewWithNodes();
					for (PutlockerNode node : nodesToAdd) {
						_adapter.add(node);
					}
					
					for (Entry<String, PutlockerNode> deleteEntry : entrySet) {
						_adapter.remove(deleteEntry.getValue());
					}
					
					if (nodesToAdd.size() > 0 || isDirtyFinal == true) {
						setListWithNodes();
					} else {
						_adapter.notifyDataSetChanged();
						_list.invalidateViews();
					}

				}
			});

		} else if (fetch instanceof PutlockerDeleteFile) {
			PutlockerDeleteFile deleteFile = (PutlockerDeleteFile) fetch;
			_list.post(new Runnable() {
				
				@Override
				public void run() {
					_deleteFileDialog.dismiss();
					new AlertDialog.Builder(PutlockerHome.this)
					.setMessage(R.string.success)
					.setTitle(R.string.file_successfully_deleted)
					.setPositiveButton(R.string.okay,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create().show();

					doUpdateFileList();
				}
			});
			
		}
	}

	@Override
	public void requestFailure(HttpFetch fetch) {
		if (fetch instanceof PutlockerDeleteFile) {
			PutlockerDeleteFile deleteFile = (PutlockerDeleteFile) fetch;
			_list.post(new Runnable() {
				@Override
				public void run() {
					_deleteFileDialog.dismiss();
					new AlertDialog.Builder(PutlockerHome.this)
							.setMessage(R.string.sorry_error_deleting_file)
							.setTitle(R.string.error_title)
							.setPositiveButton(R.string.okay,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {

										}
									}).create().show();
				}
			});
			
		}
	}

	@Override
	public void requestRetry(HttpFetch fetch) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		PutlockerNode node = _adapter.getItem(position);
		itemSelected(node);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.list_remote) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			PutlockerNode node = _adapter.getItem(info.position);
			if (node.getNodeType()
					.equals(PutlockerNode.NodeType.NODE_TYPE_FILE)) {
				menu.setHeaderTitle(node.getNodeName());
				menu.add(0, ITEM_DELETE, 0, R.string.delete_file_remote);
				_menuItemSelected = node;
			}
		}

	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == ITEM_DELETE) {
			PutlockerDeleteFile delete = new PutlockerDeleteFile(
					_menuItemSelected, getPutlockerApplication().getStorage()
							.getTyped(new AuthStorage()), this);
			ProgressDialog diag = new ProgressDialog(this);
			diag.setTitle(R.string.please_wait_delete_file_remote);
			diag.setCancelable(false);
			diag.setCanceledOnTouchOutside(false);
			diag.setMessage(getString(R.string.wait));
			diag.show();
			_deleteFileDialog = diag;
			getPutlockerApplication().getThread().mHandler.post(delete);

		}
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		PutlockerNode node = _adapter.getItem(position);
		itemSelected(node);
	}

	protected void itemSelected(PutlockerNode node) {
		if (node.getNodeType().getTypeName()
				.equals(NodeType.NODE_TYPE_FILE.getTypeName())) {
			Intent intent = new Intent(PutlockerHome.this,
					PutlockerLaunchpad.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.putExtra(PutlockerLaunchpad.DOWNLOAD_URL,
					node.getNodeDownload());
			startActivity(intent);
		} else {
			Intent intent = new Intent(PutlockerHome.this, PutlockerHome.class);
			intent.putExtra(FOLDER_ID, node.getId());
			startActivity(intent);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	protected ImageView getShareView() {
		return shareIcon;
	}

	protected ImageView getDownloadImageView() {
		return downloads;
	}

	protected ImageView getUploadImageView() {
		return uploadFile;
	}
}
