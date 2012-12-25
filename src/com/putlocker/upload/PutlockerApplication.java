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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.concurrency.LooperThread;
import com.putlocker.upload.http.CookiePersistantHttpRequest;
import com.putlocker.upload.manager.DownloadsManager;
import com.putlocker.upload.storage.Persistable;
import com.putlocker.upload.storage.PersistantStorage;
import com.putlocker.upload.storage.TypedStorageInterface;

public class PutlockerApplication extends Application {
	
	private String applicationName;
	private PersistantStorage storage;
	private DownloadService _service;
	ActivityBase currentBase = null;
	private CookiePersistantHttpRequest _cookiePersistedSession;
	private LooperThread thread;
	private static final String APPLICATION_PREFS = "PutlockerApplication.APPLICATION_PREFS";
	public static final String MX_WARN = "PutlockerApplication.MX_WARN";
	@Override
	public void onCreate() {
		super.onCreate();
		applicationName = super.getString(R.string.app_name);
		storage = new PersistantStorage(this);
		DownloadsManager manager = new DownloadsManager(storage);
		try {
			manager.markAllDownloads();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		thread = new LooperThread();
		thread.start();
	}
	
	public TypedStorageInterface<Persistable> getStorage()
	{
		return storage;
	}
	
	public void setService(DownloadService service) {
		_service = service;
	}
	/**
	 * This will retrieve the service that will be 
	 * @return This will get the currently running service or null if no service is available
	 */
	public DownloadService getService()
	{
		return _service;
	}
	
	public void setCurrentActivity(ActivityBase base)
    {
    	currentBase = base;
    }
    
    public void unsetCurrentActivity(ActivityBase base)
    {
    	if ( base == currentBase ) {
    		currentBase = null;
    	}
    }
    
    public ActivityBase getCurrentActivityBase()
    {
    	return currentBase;
    }
    
    public LooperThread getThread()
    {
    	return thread;
    }
    
    public void setSessionRequest(CookiePersistantHttpRequest req)
    {
    	_cookiePersistedSession = req;
    }
    
    public CookiePersistantHttpRequest getSessionRequest()
    {
    	return _cookiePersistedSession;
    }
    
    public SharedPreferences getKeyValue()
    {
    	return getSharedPreferences(APPLICATION_PREFS, Context.MODE_PRIVATE);
    }
}
