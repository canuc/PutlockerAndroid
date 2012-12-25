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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class PersistantStorage implements TypedStorageInterface<Persistable> {
	
	public Set<String> tables = new HashSet<String>();
	
	class PersistantHelper extends SQLiteOpenHelper {
		private static final int VERSION_NUMBER = 2;
		private static final String DATABASE_NAME = "database.db";
		public PersistantHelper(Context context) {
			super(context, DATABASE_NAME, null, VERSION_NUMBER);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			// We are going to lazily create the tables that we need
			
		}
		
		@Override
		public void onOpen(SQLiteDatabase arg0)
		{
			tables = getTables(arg0);
		}

		protected Set<String> getTables(SQLiteDatabase db)
		{
			Set< String> map = new HashSet< String>();
			// query for all the available tables 
			Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master", null);
			if(cursor!=null) {
			    	int tableColumn = cursor.getColumnIndex("tbl_name");
			    	while (cursor.moveToNext()) {
			    		map.add(cursor.getString(tableColumn));
			    	}
			    	cursor.close();
			}
			return map;
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if ( oldVersion == 1 && newVersion == 2) {
				Set< String> map = getTables(db);
				if (map.contains(PutlockerNode.tableName())) {
					// We are going to update for the new nodes containing the date 
					db.execSQL("ALTER TABLE `"+PutlockerNode.tableName()+"` ADD `" + PutlockerNode.NODE_KEY_UPLOAD_DATE + "` STRING" );
					
					db.execSQL("ALTER TABLE `"+PutlockerNode.tableName()+"` ADD `" + PutlockerNode.NODE_KEY_FILE_SIZE + "` STRING");
					
					db.execSQL("ALTER TABLE `"+PutlockerNode.tableName()+"` ADD `" + PutlockerNode.NODE_KEY_DIRTY + "` STRING DEFAULT 1");
					
				}
			}
		}
		
	}
	
	PersistantHelper helper;
	
	protected SQLiteDatabase getDatabaseWithPersistable(Persistable p)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		if ( !tables.contains(p.getTableName())) {
			db.execSQL(p.createTable());
			tables.add(p.getTableName());
		}
		return db;
	}
	public PersistantStorage(Context context)
	{	
		helper = new PersistantHelper(context);
	}
	
	public Persistable storeTyped(String key, Persistable value) {
		SQLiteDatabase db = getDatabaseWithPersistable(value);
		
		
		db.execSQL(value.getInsertForEntry());
		if ( value.isAutoIncrement() ) {
			Cursor c = db.rawQuery("SELECT last_insert_rowid()",null);
			if ( c.moveToFirst() ) {
				int autoIncrementId = c.getInt(0);
				value.setId(autoIncrementId);
			}
		}
		return null;
	}
	
	public<T extends Persistable> T getTyped(T persistable)
	{
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		Cursor c = db.rawQuery(persistable.retrieve(), null); 
		T ret = null;
		List<T> persist = new ArrayList<T>();
		if ( c.moveToNext() ) {
			persistable.parseResult(c);
			ret = persistable;
		} 
		c.close();
		
		return ret;
	}

	public<T extends Persistable> List<T> getTyped(Class<T> itemClass) throws InstantiationException, IllegalAccessException
	{
		Persistable persistable = itemClass.newInstance();
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		Cursor c = db.rawQuery(persistable.retrieveAll(), null); 

		List<T> persist = new ArrayList<T>();
		while ( c.moveToNext() ) {
			T newPersist = itemClass.newInstance();
			newPersist.parseResult(c);
			persist.add((T) newPersist);
		} 
		c.close();
		return persist;
	}
	
	public<T extends Persistable> List<T> getTypedWhere(Class<T> itemClass,String key,String value) throws InstantiationException, IllegalAccessException
	{
		Persistable persistable = itemClass.newInstance();
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		Cursor c = db.rawQuery(persistable.getSelectAllWhere(key,value), null); 
		
		List<T> persist = new ArrayList<T>();
		while ( c.moveToNext() ) {
			T newPersist = itemClass.newInstance();
			newPersist.parseResult(c);
			persist.add((T) newPersist);
		} 
		c.close();
		return persist;
	}
	
	@Override
	public<T extends Persistable> void UpdateTyped(T persistable) 
	{
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		String updateValue = persistable.getUpdateForValue();
		db.execSQL(updateValue); 
	}
	
	@Override
	public<T extends Persistable> void deleteTyped(T persistable)
	{
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		String deleteValueString = persistable.deleteItem();
		
		db.execSQL(deleteValueString);
	}

	public<F extends Persistable> void deleteTable(F persistable)
	{
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		String droptableString = persistable.deleteTable();
		
		db.execSQL(droptableString);
		tables.remove(persistable.getTableName());
	}
	@Override
	public <F extends Persistable> void deleteWhere(F persistable,
			String whereClause) {
		SQLiteDatabase db = getDatabaseWithPersistable(persistable);
		String deleteItemsWhere = persistable.deleteItemWhere(whereClause);
		Log.e("JULIAN",deleteItemsWhere);
		db.execSQL(deleteItemsWhere);
	}
}
