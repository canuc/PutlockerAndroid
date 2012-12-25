package com.putlocker.upload;

public class Constants {
	public static final String BASE_URL = "http://www.putlocker.com";
	public static final String AUTHENTICATE_PAGE = "/authenticate.php?login";
	public static final String SIGNUP_PAGE = "/authenticate.php?signup";
	public static final String FILE_LIST_URL = "/cp.php";
	public static final String CAUTH_COOKIE = "cauth";
	public static final String AUTH_COOKIE = "auth";
	public static final String UPLOAD_FORM = "/upload_form.php";
	public static final String UPLOAD_FILE_LOCATION = "/upload.php";
	public static final String UPLOAD_DOMAIN = "http://upload2.putlocker.com";
	public static final String DELETE_FILE = "/cp.php?delete=";
	
	public static final String PUTLOCKER_EXPRESSION_URL = "http://www.putlocker.com/file/.*";
	public static final String SOCKSHARE_EXPRESSION_URL = "http://www.sockshare.com/file/.*";
}
