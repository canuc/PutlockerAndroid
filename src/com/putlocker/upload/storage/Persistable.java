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

import java.util.List;

import android.database.Cursor;

public abstract class Persistable {
	
	public Persistable(Cursor cursor) 
	{
		parseResult(cursor);
	}
	
	public Persistable()
	{
		
	}
	/**
	 * This will return the table name that corresponds to this persist-able
	 */
	public abstract String getTableName();
	/**
	 * This will return the id field for the persist-able
	 * @return the id
	 */
	public abstract int getId();
	/**
	 * This will return a list of keys that correspond to the persist-able
	 * @return This will return the id that is available
	 */
	public abstract List<String> getKeys();
	/**
	 * This will get the value for the specified key
	 * @param key the key to return the value for
	 * @return the string value for the specified key
	 */
	public abstract String getValueForKey(String key);
	/**
	 * This will set the current String value for the key
	 * @param key the key to set
	 * @param value  the value to set
	 */
	public abstract void setValueForKey(String key, String value);
	
	public final String getIdKey() {
		return "id";
	}
	
	public final String getInsertForEntry() {
		String insertString = "INSERT INTO ";
		insertString += wrapColoumnName(getTableName()) + " ";
		/**
		 * Up to this point we should have "INSERT INTO `%tablename%` 
		 */
		insertString += openBrace();
		/**
		 * Now begin getting the keys that are necessary, for the proccess
		 */
		List<String> keys = getKeys();
		boolean isFirst = true;
		for( String currentKey : keys ) {
			
			if ( !isFirst ) {
				insertString += divider();
			}
			
			isFirst = false;
			
			insertString += wrapColoumnName(currentKey);
		}
		
		if ( ! isAutoIncrement() ) {
			if ( !isFirst) {
				insertString += divider();
			}
			insertString +=  wrapColoumnName(getIdKey());
		}
		insertString += closeBrace();
		/**
		* Up to this point we should have "INSERT INTO `%tablename%` SET ( `%key1%` , `%key2` , ... , %keyn% ) 
		*/
		
		insertString += values();
		
		insertString += openBrace();
		isFirst = true;
		
		// We are going to go through all the keys and add them in to the string
		for (String currentKey: keys ) {
			if ( !isFirst ) {
				insertString += divider();
			}
			
			isFirst = false;
			insertString += sanitizeColumnValue(getValueForKey(currentKey));
		}
		
		if ( !isAutoIncrement() ) {
			if ( !isFirst) {
				insertString += divider();
			}
			insertString +=  sanitizeColumnValue(getId());
		}
		
		insertString += closeBrace();
		/**
		* Up to this point we should have "INSERT INTO `%tablename%` SET ( `%key1%` , `%key2` , ... , %keyn% ) 
		*/
		return insertString;
		
	}
	
	public final String getUpdateForValue() 
	{
		String updateString = "UPDATE " + wrapColoumnName(getTableName()) + getSetName();
		List<String> keys = getKeys();
		boolean isFirst = true;
		for( String currentKey : keys ) {
			if (isFirst) {
				isFirst = false;
			} else {
				updateString += divider();
			}
			updateString += wrapColoumnName(currentKey) + " = " + sanitizeColumnValue(getValueForKey(currentKey));
		}
		
		updateString += getWhereName();
		
		updateString += wrapColoumnName(getIdKey()) + " = ";
		updateString += sanitizeColumnValue(getId());
		
		return updateString;
	}
	
	public final String getSelectAllWhere(String key,String value)
	{
		return "SELECT * from `"+getTableName()+"`" + " WHERE `"+key+"`= \""+value+"\"";
	}
	
	public String createTable() 
	{
		String creation = "CREATE TABLE `" + getTableName() + "` (`"+getIdKey()+"` INTEGER PRIMARY KEY";

		if (isAutoIncrement()) {
			creation += uniqueArgument();
		}
		
		List<String> columnNames = getKeys();
		
		for ( String column : columnNames) {
			creation += " ,`" + column + "` STRING ";
		}
		
		creation += ");";
		
		return creation;
	}
	
	public String deleteItem()
	{
		return "DELETE from `"+getTableName()+"` WHERE `" + getIdKey() + "`=" +getId();
	}
	
	public String deleteItemWhere(String where)
	{
		return "DELETE from `"+getTableName()+"` WHERE " + where;
	}
	
	public String deleteTable()
	{
		return "DROP TABLE IF EXISTS `"+getTableName()+"`";
	}
	
	public abstract void parseResult(Cursor cursor);
	
	
	public String retrieve()
	{
		
		String ret = "SELECT * from `"+getTableName()+"`" + " WHERE `"+getIdKey()+"`="+String.valueOf(getId());
		
		return ret;
	}
	
	public void setId(int id) {
		
	}
	
	public String retrieveAll()
	{
		return "SELECT * from `"+getTableName()+"`";
	} 
	
	private static String wrapColoumnName(String name )
	{
		return " `"+ name + "` ";
	}
	
	private static String sanitizeColumnValue( String value ) 
	{
		return " \""+value+"\" ";
	}
	
	private static String sanitizeColumnValue( int value ) {
		return String.valueOf(value);
	}
	
	private final static String getSetName() 
	{
		return " SET ";
	}
	
	private final String getWhereName() 
	{
		return " WHERE ";
	}
	
	private final static String openBrace()
	{
		return " ( ";
	}
	
	private final static String closeBrace() 
	{
		return " ) ";
	}
	
	private final static String divider()
	{
		return " , ";
	}
	
	private final static String values()
	{
		return " VALUES ";
	}
	
	private final static String uniqueArgument()
	{
		return " AUTOINCREMENT ";
	}
	
	public boolean isAutoIncrement()
	{
		return false;
	}
}
