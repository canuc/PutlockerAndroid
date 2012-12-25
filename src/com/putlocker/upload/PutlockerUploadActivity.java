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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.concurrency.LooperSetUp;
import com.putlocker.upload.concurrency.LooperThread;
import com.putlocker.upload.http.CaptchaImageFetch;
import com.putlocker.upload.http.CookiePersistantHttpRequest;
import com.putlocker.upload.http.HttpFetch;
import com.putlocker.upload.http.LoginHttpFetch;
import com.putlocker.upload.http.RequestCallback;
import com.putlocker.upload.http.RequestCaptchaLocationHttpFetch;
import com.putlocker.upload.storage.AuthStorage;

/**
 * This will be the initial login that we need to do
 * the after we will save the data that is required
 * 
 * @author julian
 *
 */
public class PutlockerUploadActivity extends ActivityBase implements RequestCallback {
	
	private ImageView _captchaImageView;
	private View _imageFrameView;
	private View _spinner;
	private CookiePersistantHttpRequest _store;
	private Button _loginButton;
	private Button _registerButton;
	private ProgressDialog _progressDialog;
	/**
	 * Form values
	 */
	private EditText _username;
	private EditText _password;
	private EditText _captcha;
	
	private final static int START_REGISTER_ACTIVTY = 100;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        _captchaImageView = (ImageView) findViewById(R.id.captcha);
        _imageFrameView = findViewById(R.id.capcha_frame);
        _spinner = (View) findViewById(R.id.progress_spinner);
        _registerButton = (Button) findViewById(R.id.register);
        _loginButton = (Button) findViewById(R.id.login);
        _username = (EditText) findViewById(R.id.putlocker_username);
        _password = (EditText) findViewById(R.id.putlocker_password);
        _captcha = (EditText) findViewById(R.id.putlocker_captcha);
        
        _loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( _username.length() == 0 )
				{
					showErrorDialog(R.string.empty_username);
				} else if ( _password.length() == 0 ) {
					showErrorDialog(R.string.empty_password);
				} else if ( _captcha.length() == 0) {
					showErrorDialog(R.string.empty_capcha);
				} else {
					_loginButton.setEnabled(false);
					LoginHttpFetch loginFetch = new LoginHttpFetch(_store,PutlockerUploadActivity.this, _username.getText().toString(), _password.getText().toString(), _captcha.getText().toString());
					getPutlockerApplication().getThread().mHandler.post(loginFetch);
					_progressDialog = ProgressDialog.show(PutlockerUploadActivity.this, "Logging in...","Please wait while we log you in..");
				}
			}
		});
        
        _registerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent registerIntent = new Intent(PutlockerUploadActivity.this,RegisterActivity.class);
				startActivityForResult(registerIntent,START_REGISTER_ACTIVTY);
			}
		});
        
        final RequestCaptchaLocationHttpFetch fetch = new RequestCaptchaLocationHttpFetch(this,Constants.BASE_URL+Constants.AUTHENTICATE_PAGE);
        getPutlockerApplication().getThread().setLooperStartedCallback(new LooperSetUp() {
			
			@Override
			public void LooperStarted() {
				getPutlockerApplication().getThread().mHandler.post(fetch);
			}
		});
    }
    
    
    
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
    
    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == START_REGISTER_ACTIVTY ) {
            if (resultCode == RegisterActivity.RESULT_CODE_FINNISH ) {
            	finish();
            }
        }
    }

	@Override
	public void requestSuccess(HttpFetch fetch) {
		if ( fetch instanceof RequestCaptchaLocationHttpFetch ) {
			RequestCaptchaLocationHttpFetch tmpfetch = (RequestCaptchaLocationHttpFetch) fetch;
			_store = tmpfetch;
			CaptchaImageFetch captchaFetch = new CaptchaImageFetch(tmpfetch, tmpfetch.getCaptchaLocation(), null,this);
			getPutlockerApplication().getThread().mHandler.post(captchaFetch);
		}else if ( fetch instanceof CaptchaImageFetch) {
			final CaptchaImageFetch tmpfetch = (CaptchaImageFetch) fetch;
			_captchaImageView.post(new Runnable() {
				@Override
				public void run() {
					_spinner.setVisibility(View.GONE);
					_imageFrameView.setVisibility(View.VISIBLE);
					_captchaImageView.setImageBitmap(tmpfetch.getCatchaBitmap());
				}
			});
		} else if ( fetch instanceof LoginHttpFetch ) {
			// the login fetch 
			_progressDialog.dismiss();
			_progressDialog = null;
			LoginHttpFetch loginFetch = (LoginHttpFetch) fetch;
			final AlertDialog dialog = new AlertDialog.Builder(this).setMessage("You have been logged in").create();
			dialog.setTitle("Success");
			dialog.setCancelable(false);
			dialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.dismiss();
			    	  Intent newIntent = new Intent(PutlockerUploadActivity.this,PutlockerHome.class);
			    	  startActivity(newIntent);
			    	  finish();
			    } });
			dialog.show();
			AuthStorage storageValue = new AuthStorage();
			
			storageValue.setValueForKey(AuthStorage.AUTH_PASSWORD, ((LoginHttpFetch) fetch).getPassword());
			storageValue.setValueForKey(AuthStorage.AUTH_AUTHCODE, loginFetch.getAuthToken());
			storageValue.setValueForKey(AuthStorage.AUTH_USERNAME, loginFetch.getUsername());
			storageValue.setValueForKey(AuthStorage.AUTH_CAUTHTOKEN, loginFetch.getCAuthToken());
			
			PutlockerApplication app = getPutlockerApplication();
			app.getStorage().storeTyped("login", storageValue);
		}
	}

	@Override
	public void requestFailure(HttpFetch fetch) {
		if ( fetch instanceof RequestCaptchaLocationHttpFetch || fetch instanceof CaptchaImageFetch ) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					final AlertDialog dialog = new AlertDialog.Builder(PutlockerUploadActivity.this).setMessage("Cannot reach www.putlocker.com").create();
					dialog.setCancelable(false);
					dialog.setButton("OK", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  dialog.dismiss();
					    } });
					dialog.show();
				}
			});
		} else if ( fetch instanceof LoginHttpFetch ) {
			final LoginHttpFetch loginFetch = (LoginHttpFetch) fetch;
			_loginButton.post(new Runnable() {
				
				@Override
				public void run() {
					String message = loginFetch.getErrorString();
					
					if ( message == null || message.length() == 0) {
						message = "Invalid Credentials or Captcha input";
					}
					
					final AlertDialog dialog = new AlertDialog.Builder(PutlockerUploadActivity.this).setMessage(message).create();
					dialog.setCancelable(false);
					dialog.setButton("OK", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  dialog.dismiss();
					    } });
					_progressDialog.dismiss();
					_progressDialog = null;
					dialog.show();
					_spinner.setVisibility(View.VISIBLE);
					_imageFrameView.setVisibility(View.GONE);
					_captcha.setText("");
					_loginButton.setEnabled(true);
				}
			});
			/**
			 * On failure, we are going to re-get the captch input
			 */
			final RequestCaptchaLocationHttpFetch fetchRequest = new RequestCaptchaLocationHttpFetch(this,Constants.BASE_URL+Constants.AUTHENTICATE_PAGE);
			getPutlockerApplication().getThread().setLooperStartedCallback(new LooperSetUp() {
				
				@Override
				public void LooperStarted() {
					
					getPutlockerApplication().getThread().mHandler.post(fetchRequest);
					
				}
			});
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public void requestRetry(HttpFetch fetch) {
		
	}
    
    
}