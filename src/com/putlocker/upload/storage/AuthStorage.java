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

import android.database.Cursor;

public class AuthStorage extends Persistable {

	String username;
	String password;
	String authcode;
	String cauth;

	private final String AUTH_CODE_ID = "id";
	public final static String AUTH_PASSWORD = "password";
	public final static String AUTH_AUTHCODE = "authcode";
	public final static String AUTH_USERNAME = "username";
	public final static String AUTH_CAUTHTOKEN = "cauth";
	
	@Override
	public String getTableName() {
		return "table_auth";
	}

	@Override
	public int getId() {
		return 1;
	}

	@Override
	public List<String> getKeys() {
		// TODO Auto-generated method stub
		List<String> retList = new LinkedList<String>();
		retList.add(AUTH_PASSWORD);
		retList.add(AUTH_USERNAME);
		retList.add(AUTH_AUTHCODE);
		retList.add(AUTH_CAUTHTOKEN);
		return retList;
	}

	@Override
	public String getValueForKey(String key) {
		String retValue = "";
		
		if ( key.equals(AUTH_CODE_ID) ) {
			retValue = "1";
		} else if ( key.equals(AUTH_PASSWORD) ) {
			retValue = password;
		} else if ( key.equals(AUTH_USERNAME) ) {
			retValue = username;
		} else if ( key.equals(AUTH_AUTHCODE) ) {
			retValue = authcode;
		} else if ( key.equals(AUTH_CAUTHTOKEN)){
			retValue = cauth;
		} else {
			throw new RuntimeException("Runtime exeption cannot find key " + key + " for class " + getClass().getName() );
		}
		
		return retValue;
	}

	@Override
	public void setValueForKey(String key, String value) 
	{
		
		if ( key.equals(AUTH_PASSWORD) ) {
			password = value;
		} else if ( key.equals(AUTH_USERNAME) ) {
			username = value;
		} else if ( key.equals(AUTH_AUTHCODE) ) {
			authcode = value;
		} else if ( key.equals(AUTH_CAUTHTOKEN)){
			cauth = value;
		} else  {
			throw new RuntimeException("Runtime exeption cannot find key " + key + " for class " + getClass().getName() );
		}
		
		return;
	}

	@Override
	public void parseResult(Cursor cursor) {
		int password_index = cursor.getColumnIndex(AUTH_PASSWORD);
		int auth_index = cursor.getColumnIndex(AUTH_AUTHCODE);
		int username_index = cursor.getColumnIndex(AUTH_USERNAME);
		int cauth_index = cursor.getColumnIndex(AUTH_CAUTHTOKEN);
		
		username = cursor.getString(username_index);
		password = cursor.getString(password_index);
		authcode = cursor.getString(auth_index);
		cauth = cursor.getString(cauth_index);
		
		return;
	}

	
	
	

}
