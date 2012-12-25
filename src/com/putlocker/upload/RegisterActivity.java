package com.putlocker.upload;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.putlocker.upload.activity.ActivityBase;
import com.putlocker.upload.concurrency.LooperThread;
import com.putlocker.upload.http.BasicSessionInitilization;
import com.putlocker.upload.http.CookiePersistantHttpRequest;
import com.putlocker.upload.http.HttpFetch;
import com.putlocker.upload.http.RegisterHttpRequest;
import com.putlocker.upload.http.RequestCallback;
import com.putlocker.upload.storage.AuthStorage;

public class RegisterActivity extends ActivityBase implements RequestCallback {

	private static final String USERNAME_EDITTEXT_BUNDLE_KEY = "RegisterActivity.username";
	private static final String PASSWORD_EDITTEXT_BUNDLE_KEY = "RegisterActivity.password";
	private static final String PASSWORD2_EDITTEXT_BUNDLE_KEY = "RegisterActivity.password2";
	private static final String EMAIL_EDITTEXT_BUNDLE_KEY = "RegisterActivity.email";

	private static final String USERNAME_PATTERN = "^[_A-Za-z0-9_-]{5,20}$";
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String PASSWORD_PATTERN = "^[a-z0-9_-]{5,20}$";

	EditText usernameField;
	EditText passwordField;
	EditText emailField;

	private String usernameStore;
	private String passwordStore;
	private String emailStore;

	CookiePersistantHttpRequest initialRequest;

	Button _registerButton;
	static LooperThread th;

	AlertDialog _progressDialog;
	
	public static final int RESULT_CODE_FINNISH = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_activity);

		usernameField = (EditText) findViewById(R.id.putlocker_register_username);
		passwordField = (EditText) findViewById(R.id.putlocker_register_password);
		emailField = (EditText) findViewById(R.id.putlocker_register_email);

		if (savedInstanceState != null) {
			usernameField.setText(savedInstanceState.getString(USERNAME_EDITTEXT_BUNDLE_KEY) == null ? "" :savedInstanceState.getString(USERNAME_EDITTEXT_BUNDLE_KEY));
			passwordField.setText(savedInstanceState.getString(
					PASSWORD_EDITTEXT_BUNDLE_KEY)== null ? "" :savedInstanceState.getString(PASSWORD_EDITTEXT_BUNDLE_KEY));
			emailField.setText(savedInstanceState
					.getString(EMAIL_EDITTEXT_BUNDLE_KEY)== null ? "" :savedInstanceState.getString(EMAIL_EDITTEXT_BUNDLE_KEY));
		}
		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle("Register new account");
		_registerButton = (Button) findViewById(R.id.register_account_button);

		_registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				verifyRegistration();
			}
		});
		if (th == null) {
			th = new LooperThread();
			th.start();
		}
	}

	protected void verifyRegistration() {
		String registerUsername = usernameField.getText().toString();
		String registerPassword = passwordField.getText().toString();
		String registerEmail = emailField.getText().toString();

		Pattern p = Pattern.compile(EMAIL_PATTERN);
		Matcher match = p.matcher(registerEmail);

		if (!match.matches()) {
			AlertDialog dialog = (new AlertDialog.Builder(this)
					.setTitle("Invalid email")
					.setMessage("The email you entered is not a proper email"))
					.create();
			dialog.show();
			return;
		}

		p = Pattern.compile(USERNAME_PATTERN);
		Matcher usernameMatcher = p.matcher(registerUsername);
		if (!usernameMatcher.matches()) {
			AlertDialog dialog = (new AlertDialog.Builder(this)
					.setTitle("Invalid username")
					.setMessage("The username must be more then 3 characters and not contain any special characters"))
					.create();
			dialog.show();
			return;
		}

		Matcher passMatcher = p.matcher(registerPassword);
		if (!passMatcher.matches()) {
			AlertDialog dialog = (new AlertDialog.Builder(this)
					.setTitle("Invalid password")
					.setMessage("The password must be more then 3 characters and not contain any special characters"))
					.create();
			dialog.show();
			return;
		}

		// we are home free - we can attempt to register
		usernameStore = registerUsername;
		passwordStore = registerPassword;
		emailStore = registerEmail;
		_registerButton.setEnabled(false);
		BasicSessionInitilization request = new BasicSessionInitilization(
				this);
		_progressDialog = ProgressDialog.show(RegisterActivity.this, "Logging in...","Please wait while we register the account");
		th.mHandler.post(request);
	}

	@Override
	public void requestSuccess(HttpFetch fetch) {
		if (fetch instanceof BasicSessionInitilization) {
			BasicSessionInitilization tmpfetch = (BasicSessionInitilization) fetch;
			initialRequest = tmpfetch;
			RegisterHttpRequest register = new RegisterHttpRequest(this,
					tmpfetch, usernameStore, passwordStore, emailStore);
			th.mHandler.post(register);
		} else if (fetch instanceof RegisterHttpRequest) {
		
			// Now we should get the auth cookie that is stored
			RegisterHttpRequest register = (RegisterHttpRequest) fetch;
			// We are going to take all of our authentication tokens and we
			AuthStorage authStorage = new AuthStorage();
			authStorage.setValueForKey(AuthStorage.AUTH_AUTHCODE,
					register.getAuthToken());
			authStorage.setValueForKey(AuthStorage.AUTH_PASSWORD,
					register.getPassword());
			authStorage.setValueForKey(AuthStorage.AUTH_USERNAME,
					register.getUsername());
			authStorage.setValueForKey(AuthStorage.AUTH_CAUTHTOKEN,
					register.getCAuthToken());

			getPutlockerApplication().getStorage().storeTyped("something",
					authStorage);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_progressDialog.dismiss();
					_registerButton.setEnabled(true);
					Intent intent = new Intent(RegisterActivity.this,
							PutlockerHome.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
					setResult(RESULT_CODE_FINNISH);
					startActivity(intent);
					finish();
				}
			});
		}

	}

	@Override
	public void requestFailure(HttpFetch fetch) {
		if (fetch instanceof BasicSessionInitilization) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_progressDialog.dismiss();
					final AlertDialog dialog = new AlertDialog.Builder(RegisterActivity.this)
							.setMessage("Cannot reach www.putlocker.com")
							.create();
					dialog.setCancelable(false);
					dialog.setButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					dialog.show();
					// we are home free - we can attempt to register
					usernameStore = null;
					passwordStore = null;
					emailStore = null;
					_registerButton.setEnabled(true);

				}
			});
		} else if (fetch instanceof RegisterHttpRequest ){
			final RegisterHttpRequest register = (RegisterHttpRequest) fetch;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_progressDialog.dismiss();
					String message = register.getErrorString();
					if ( message == null || message.length() == 0 ) {
						message = "Sorry that username has been taken";
					}
					final AlertDialog dialog = new AlertDialog.Builder(
							RegisterActivity.this).setMessage(
							message).create();
					dialog.setCancelable(false);
					dialog.setButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					dialog.show();

					_registerButton.setEnabled(true);
				}
			});
		}

		return;
	}

	@Override
	public void requestRetry(HttpFetch fetch) {

	}

}
