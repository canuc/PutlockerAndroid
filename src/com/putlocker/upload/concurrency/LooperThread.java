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

package com.putlocker.upload.concurrency;

import android.os.Handler;
import android.os.Looper;

public class LooperThread extends Thread {
	public Handler mHandler;
	public LooperSetUp _setUpCallback = null;
	private boolean _threadSetup = false;
	public synchronized void setLooperStartedCallback(LooperSetUp callback) {
		if ( _threadSetup == false ) {
			_setUpCallback = callback;
		} else {
			callback.LooperStarted();
		}
	}
	
	public  void run() {
		Looper.prepare();
		mHandler = new Handler();
		synchronized (this) {
			if ( _setUpCallback != null ) {
				_setUpCallback.LooperStarted();
			}
			_threadSetup = true;
		}
		// Make sure that we are going to reset the value on the setup callback
		// because we dont want the callback to be reset
		_setUpCallback = null;
		Looper.loop();
	}

}
