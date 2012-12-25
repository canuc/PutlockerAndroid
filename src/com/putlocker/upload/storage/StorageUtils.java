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


public class StorageUtils {
	
	
	public enum StorageType {
		STORAGE_TYPE_PERSISTANT,
		STORAGE_TYPE_SESSION
	};
	
	private String _storageDomain;
	/**
	 * Here we are going to intialize a storage class 
	 */
	public StorageUtils(String storageDomain) {
		_storageDomain = storageDomain;
	}
	
}
