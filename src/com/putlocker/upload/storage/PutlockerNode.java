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
package com.putlocker.upload.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.putlocker.upload.Constants;

import android.database.Cursor;

public class PutlockerNode extends Persistable  {
	public static final String NODE_TABLE = "nodes";
	
	public static final String NODE_NAME_KEY ="nodename";
	public static final String NODE_TYPE_KEY ="nodetype";
	public static final String NODE_KEY = "nodekey";
	public static final String NODE_PARENT_KEY = "parent";
	public static final String NODE_KEY_UPLOAD_DATE = "upload_date";
	public static final String NODE_KEY_FILE_SIZE = "file_size";
	public static final String NODE_KEY_DIRTY = "dirty";
	
	public static final String tableName () {
		return NODE_TABLE;
	}
	public enum NodeType {
		NODE_TYPE_FILE("file"),
		NODE_TYPE_FOLDER("folder");
		
		private String _nodeName;
		
		NodeType(String nodeName)
		{
			_nodeName = nodeName;
		}
		
		public String getTypeName() {
			return _nodeName;
		}
		
		public static NodeType getTypeFromString(String type) {
			
			if ( type.equals(NODE_TYPE_FOLDER.getTypeName())) {
				return NodeType.NODE_TYPE_FOLDER;
			} else if ( type.equals(NODE_TYPE_FILE.getTypeName())) {
				return NodeType.NODE_TYPE_FILE;
			}
			return null;
		}
		
	};
	
	private int _id;
	private String _name;
	private String _key;
	private NodeType _type;
	private int _parent;
	private String _fileSize;
	private String _uploadDate;
	private boolean _isDirty;
	
	public PutlockerNode()
	{
		
	}
	
	public PutlockerNode( int id ) {
		_id = id;
	}
	
	public PutlockerNode(Cursor c) {
		super(c);
	}

	
	public PutlockerNode(NodeType type,String name, String key, String filesize, String uploadDate, int parent , boolean dirty)
	{
		_name = name;
		_key = key;
		_type = type;
		_parent = parent;
		_uploadDate = uploadDate;
		_fileSize = filesize;
		_isDirty = dirty;
	}
	
	@Override
	public void setId(int id)
	{
		_id = id;
	}
	
	
	@Override
	public String getTableName() {
		return NODE_TABLE;
	}


	@Override
	public List<String> getKeys() {
		List<String> strList = new LinkedList<String>();
		strList.add(NODE_KEY);
		strList.add(NODE_NAME_KEY);
		strList.add(NODE_TYPE_KEY);
		strList.add(NODE_PARENT_KEY);
		strList.add(NODE_KEY_FILE_SIZE);
		strList.add(NODE_KEY_UPLOAD_DATE);
		strList.add(NODE_KEY_DIRTY);
		return strList;
	}

	@Override
	public String getValueForKey(String key) {
		String value = null;
		if ( key.equals(NODE_NAME_KEY)) {
			value = _name;
		} else if ( key.equals(NODE_TYPE_KEY)) {
			value = _type.getTypeName();
		} else if ( key.equals(NODE_KEY) ) {
			value = _key;
		} else if ( key.equals(NODE_PARENT_KEY) ) {
			value = String.valueOf(_parent);
		} else if ( key.equals(NODE_KEY_FILE_SIZE)) {
			value = _fileSize;
		} else if ( key.equals(NODE_KEY_UPLOAD_DATE) ) {
			value = _uploadDate;
		} else if ( key.equals(NODE_KEY_DIRTY) ) {
			value = _isDirty ? "1" : "0";
		}
		return value;
	}

	@Override
	public void setValueForKey(String key, String value) {
		if ( key.equals(NODE_NAME_KEY)) {
			_name = value;
		} else if ( key.equals(NODE_TYPE_KEY)) {
			 _type = NodeType.getTypeFromString(value);
		} else if ( key.equals(NODE_KEY) ) {
			_key = value;
		} else if ( key.equals(NODE_PARENT_KEY) ) {
			_parent = Integer.parseInt(value);
		} else if ( key.equals(NODE_KEY_UPLOAD_DATE)) {
			_uploadDate = value;
		} else if ( key.equals(NODE_KEY_FILE_SIZE) ) {
			_fileSize = value;
		} else if ( key.equals(NODE_KEY_DIRTY )) {
			_isDirty = "1".equals(value) ? true : false;
		}
		return;
	}

	@Override
	public void parseResult(Cursor cursor) {
		int index_name = cursor.getColumnIndex(NODE_NAME_KEY);
		int index_type = cursor.getColumnIndex(NODE_TYPE_KEY);
		int index_key = cursor.getColumnIndex(NODE_KEY);
		int index_id = cursor.getColumnIndex(getIdKey());
		int index_parent = cursor.getColumnIndex(NODE_PARENT_KEY);
		int index_file_size_index = cursor.getColumnIndex(NODE_KEY_FILE_SIZE);
		int index_file_upload_date_index = cursor.getColumnIndex(NODE_KEY_UPLOAD_DATE);
		int index_file_dirty_index = cursor.getColumnIndex(NODE_KEY_DIRTY);
		
		_name = cursor.getString(index_name);
		_type = NodeType.getTypeFromString(cursor.getString(index_type));
		_key = cursor.getString(index_key);
		_id = cursor.getInt(index_id);
		_parent = cursor.getInt(index_parent);
		_fileSize = cursor.getString(index_file_size_index);
		_uploadDate = cursor.getString(index_file_upload_date_index);
		_isDirty = cursor.getInt(index_file_dirty_index) == 0 ? false : true;
	}

	
	@Override
	public boolean isAutoIncrement()
	{
		return true;
	}
	
	@Override 
	public int getId()
	{
		return _id;
	}
	
	public NodeType getNodeType()
	{
		return _type;
	}
	
	public String getNodeName()
	{
		return _name;
	}
	
	public String getNodeKey()
	{
		return _key;
	}
	
	public boolean isDirty() 
	{
		return _isDirty;
	}
	
	public String getNodeDownload()
	{
		if ( _type.getTypeName().equals(NodeType.NODE_TYPE_FILE.getTypeName())) {
			return Constants.BASE_URL+"/file/"+getNodeKey();
		}
		
		return null;
	}
}
