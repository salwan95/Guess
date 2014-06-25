package com.example.guess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.fedorvlasov.lazylist.ImageLoader;
import com.facebook.android.Facebook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import eu.janmuller.android.simplecropimage.CropImage;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class Login extends Activity {

	static final String APP_ID = "264964670355492";
	static final String ADMIN_ID = "894896947193437";
	static final String PANDORA_ID = "272788702826820";
	static final String TESTGRROUP_ID = "346826208792378";
	static final String PENDING_REQUEST_BUNDLE_KEY = "com.example.guess.Login:PendingRequest";

	public static final int REQUEST_CODE_GALLERY = 0x1;
	public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
	public static final int REQUEST_CODE_CROP_IMAGE = 0x3;
	public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	private File mFileTemp;

	private Session session;
	private Button loginButton;
	private Button logoutButton;
	private Button startButton;
	private Button parseButton;
	private Button takePhotoButton;
	private Button uploadPhotoButton;

	private ProgressDialog progressDialog;
	// private TextView fullscreenContent;
	private ParseImageView userProfilePic;
	// private ProfilePictureView userProfilePictureView;
	private JSONArray jsonArrayGroupMembers;

	private ConnectionDetector mConnectionDetector;
	private ImageLoader imageLoader;

	private boolean pendingRequest;
	private boolean isLoggedIn;
	private boolean isLoggedInToday;
	private boolean isAdmin;
	private boolean isUpdatingParse;
	private boolean isInitializedParse;

	private ParseUser currentParseUser;
	private GroupMember currentGroupMember;

	private String currentParseUserFacebookId;
	private String currentParseUserFacebookName;
	private String currentParseUserFacebookProfilePicUrl;
	private String tempFacebookId;
	private String tempFacebookName;
	private String tempFacebookProfilePicUrl;

	public ParseUser getCurrentParseUser() {
		return currentParseUser;
	}

	public GroupMember getCurrentGroupMember() {
		return currentGroupMember;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mConnectionDetector = new ConnectionDetector(getApplicationContext());
		imageLoader = new ImageLoader(getApplicationContext());
		isInitializedParse = false;

		if (mConnectionDetector.isConnectingToInternet() && !isInitializedParse) {
			initializeParse();
			isInitializedParse = true;
		} else {
			ToastMessage.showMessage(this, ToastMessage.NO_INTERNET_CONNECTION);
		}

		initUI();
		updateUI();

		isUpdatingParse = false;
		
		

	}

	private void initializeParse() {
		ParseObject.registerSubclass(GroupMember.class);
		ParseObject.registerSubclass(GuessResult.class);
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "7ReCi9TwKqCF0iuwlnHOIBlnLplknfAIfqQ9qTdv",
				"xjdudMgyR8H0HRbI7nSJqiEVJD4wDGj5091o0r8K");
		ParseFacebookUtils.initialize(APP_ID);

		currentParseUser = ParseUser.getCurrentUser();
		isLoggedInToday = false;
		if ((currentParseUser != null)
				&& ParseFacebookUtils.isLinked(currentParseUser)) {
			// Go to the user info activity
			getUserInfo();
			isLoggedIn = true;
		} else {
			isLoggedIn = false;
			isAdmin = false;
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void onLogoutButtonClicked() {
		// Log the user out
		// ParseFacebookUtils.getSession().closeAndClearTokenInformation();
		ParseUser.logOut();
		isLoggedIn = false;
		updateUI();
	}

	private void onLoginButtonClicked() {
		progressDialog = ProgressDialog.show(Login.this, "Wait",
				"Logging in...", true);
		List<String> permissions = Arrays.asList("public_profile",
				"user_groups");
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException err) {
				progressDialog.dismiss();
				if (user == null) {
					isLoggedIn = false;
				} else if (user.isNew()) {
					Log.d("", "User signed up and logged in through Facebook!");
					getUserInfo();
					isLoggedIn = true;
				} else {
					Log.d("", "User logged in through Facebook!");
					getUserInfo();
					isLoggedIn = true;
				}
			}
		});
	}

	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// if (this.session.onActivityResult(this, requestCode, resultCode, data) &&
	// pendingRequest &&
	// this.session.getState().isOpened()) {
	// sendUserProfileRequest();
	// }
	// }
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bitmap;
		switch (requestCode) {

		case REQUEST_CODE_GALLERY:

			try {

				InputStream inputStream = getContentResolver().openInputStream(
						data.getData());
				FileOutputStream fileOutputStream = new FileOutputStream(
						mFileTemp);
				copyStream(inputStream, fileOutputStream);
				fileOutputStream.close();
				inputStream.close();

				startCropImage();

			} catch (Exception e) {

				Log.e("", "Error while creating temp file", e);
			}

			break;

		case REQUEST_CODE_CROP_IMAGE:
			String path = data.getStringExtra(CropImage.IMAGE_PATH);
			if (path == null) {
				return;
			}
			bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
			savePhoto(bitmap);
			userProfilePic.setImageBitmap(bitmap);
			break;
		default:
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode,
					data);
		}

	}

	private void savePhoto(Bitmap b) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] bitmapData = stream.toByteArray();

		ParseFile photoFile = new ParseFile("customedProfilePic.jpg",
				bitmapData);
		updateCustomedProfilePic(photoFile);
	}

	private void updateCustomedProfilePic(ParseFile photoFile) {
		final ParseFile finalPhotoFile = photoFile;
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember");
		// Retrieve the object by id
		query.whereEqualTo("facebookId", currentParseUserFacebookId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject groupMember, ParseException e) {
				// TODO Auto-generated method stub
				if (e != null) {
					Log.e("Parse", "Error saving");
				} else {
					groupMember.put("customedProfilePic", finalPhotoFile);
					groupMember.put("usingCustomedProfilePic", true);
					groupMember.saveInBackground();
				}

			}
		});
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	//
	// if (resultCode != RESULT_OK) {
	//
	// return;
	// }
	//
	// Bitmap bitmap;
	//
	// switch (requestCode) {
	//
	// case REQUEST_CODE_GALLERY:
	//
	// try {
	//
	// InputStream inputStream =
	// getContentResolver().openInputStream(data.getData());
	// FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
	// copyStream(inputStream, fileOutputStream);
	// fileOutputStream.close();
	// inputStream.close();
	//
	// startCropImage();
	//
	// } catch (Exception e) {
	//
	// Log.e(TAG, "Error while creating temp file", e);
	// }
	//
	// break;
	// case REQUEST_CODE_TAKE_PICTURE:
	//
	// startCropImage();
	// break;
	// case REQUEST_CODE_CROP_IMAGE:
	//
	// String path = data.getStringExtra(CropImage.IMAGE_PATH);
	// if (path == null) {
	//
	// return;
	// }
	//
	// bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
	// mImageView.setImageBitmap(bitmap);
	// break;
	// }
	// super.onActivityResult(requestCode, resultCode, data);
	// }

	private void getUserInfo() {
		session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
			// makeGroupRequest();
		}
	}

	private void makeMeRequest() {
		progressDialog = ProgressDialog.show(Login.this, "Wait",
				"Getting user information...", true);
		String requestId = "me";
		Bundle params = new Bundle();
		params.putString("fields", "name,id,picture.width(400).height(400)");
		Request request = new Request(ParseFacebookUtils.getSession(),
				requestId, params, HttpMethod.GET, new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						if (response != null) {
							GraphObject graphObject = response.getGraphObject();
							if (graphObject != null) {
								JSONObject userProfile = new JSONObject();
								try {
									JSONObject json = graphObject
											.getInnerJSONObject();
									currentParseUserFacebookId = json
											.getString("id");
									currentParseUserFacebookName = json
											.getString("name");
									currentParseUserFacebookProfilePicUrl = json
											.getJSONObject("picture")
											.getJSONObject("data")
											.getString("url");

									// Populate the JSON object
									userProfile.put("facebookId",
											currentParseUserFacebookId);
									userProfile.put("name",
											currentParseUserFacebookName);
									userProfile
											.put("facebookProfilePicUrl",
													currentParseUserFacebookProfilePicUrl);

									// Save the user profile info in a user
									// property
									currentParseUser = ParseUser.getCurrentUser();
									currentParseUser.put("profile", userProfile);
									if (currentParseUser.getUpdatedAt() != null) {
										Date currentDate = new Date();
										if (currentParseUser.getUpdatedAt()
												.getTime()
												- currentDate.getTime() < 24 * 3600) {
											isLoggedInToday = true;
										}
									}

									currentParseUser.saveInBackground();

									findCurrentGroupMember();
									if (currentParseUserFacebookId
											.equals(ADMIN_ID))
										isAdmin = true;

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d("",
										"The facebook session was invalidated.");
								onLogoutButtonClicked();
							} else {
								Log.d("", "Some other error: "
										+ response.getError().getErrorMessage());
							}
						}

					}
				});
		request.executeAsync();
	}

	private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
		protected Bitmap doInBackground(Void... params) {

			String profilePicUrl = currentGroupMember.getCurrentProfilePicUrl();
			if (profilePicUrl == null)
				return null;

			Bitmap profilePic = null;
			try {
				InputStream in = new java.net.URL(profilePicUrl).openStream();
				profilePic = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return profilePic;
		}

		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result == null) {
				// Show error message and tell user to upload a new one or pull
				// one from facebook
			}
			userProfilePic.setImageBitmap(result);
			updateUI();
			progressDialog.dismiss();
			if (!isLoggedInToday)
				makeGroupRequest();
		}
	}

	private void findCurrentGroupMember() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember");

		// Retrieve the object by id
		query.whereEqualTo("facebookId", currentParseUserFacebookId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject groupMember, ParseException e) {
				// TODO Auto-generated method stub
				if (groupMember != null) {
					currentGroupMember = new GroupMember(groupMember);
				} else {
					currentGroupMember = new GroupMember();
					currentGroupMember
							.setFacebookId(currentParseUserFacebookId);
					currentGroupMember.setName(currentParseUserFacebookName);
					currentGroupMember
							.setFacebookProfilePicUrl(currentParseUserFacebookProfilePicUrl);
					currentGroupMember.settUsingCustomedProfilePic(false);
					currentGroupMember.settUsingCustomedName(false);
					currentGroupMember.saveInBackground();
				}
				
//				imageLoader.DisplayImage(currentGroupMember.getCurrentProfilePicUrl(), userProfilePic);
//				updateUI();
//				progressDialog.dismiss();
//				if (!isLoggedInToday)
//					makeGroupRequest();

				// Show the user info
				new DownloadImageTask().execute();
			}
		});
	}

	private void makeGroupRequest() {
		progressDialog = ProgressDialog.show(Login.this, "Wait",
				"Gathering group information...");
		String requestId = PANDORA_ID + "/members";
		Bundle params = new Bundle();
		params.putString("fields", "name,id,picture.width(9999).height(9999)");
		Request request = new Request(ParseFacebookUtils.getSession(),
				requestId, params, HttpMethod.GET, new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						if (response != null) {
							GraphObject graphObject = response.getGraphObject();
							if (graphObject != null) {
								try {
									jsonArrayGroupMembers = graphObject
											.getInnerJSONObject().getJSONArray(
													"data");
									Log.e("Member Size",
											jsonArrayGroupMembers.length() + "");
									isUpdatingParse = true;
									new UpdateGroupMember().execute();

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
					}
				});
		pendingRequest = false;
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		pendingRequest = savedInstanceState.getBoolean(
				PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(PENDING_REQUEST_BUNDLE_KEY, pendingRequest);
	}

	/*
	 * private void loginWithFacebook() { if (session.isOpened()) {
	 * sendUserProfileRequest(); } else { StatusCallback callback = new
	 * StatusCallback() { public void call(Session session, SessionState state,
	 * Exception exception) { if (exception != null) { new
	 * AlertDialog.Builder(Login.this) .setTitle("Login failed")
	 * .setMessage(exception.getMessage()) .setPositiveButton("OK", null)
	 * .show(); Login.this.session = createSession(); } else if
	 * (session.isOpened()) { sendUserProfileRequest(); } } }; pendingRequest =
	 * true; this.session.openForRead(new
	 * Session.OpenRequest(this).setCallback(callback)); } //isLoggedIn = true;
	 * //updateUI(); }
	 * 
	 * private void sendUserProfileRequest() { String requestId = "me/picture";
	 * Bundle params = new Bundle(); params.putString("type", "large");
	 * params.putString("width", "800"); params.putString("redirect", "false");
	 * Request request = new Request(session, requestId, params, HttpMethod.GET,
	 * new Request.Callback() {
	 * 
	 * @Override public void onCompleted(Response response) { if (response !=
	 * null) { GraphObject graphObject = response.getGraphObject(); if
	 * (graphObject != null) { JSONObject json; try { json = new
	 * JSONObject(graphObject.getInnerJSONObject().getString("data")); String
	 * profilePicUrl = json.getString("url"); new
	 * DownloadImageTask().execute(profilePicUrl); } catch (JSONException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } } } } });
	 * 
	 * pendingRequest = false; RequestAsyncTask task = new
	 * RequestAsyncTask(request); task.execute(); } private void
	 * sendGroupMembersRequest() { final ProgressDialog progressDialog =
	 * ProgressDialog.show(Login.this, "Wait",
	 * "Gathering group information..."); String requestId =
	 * "272788702826820/members"; Bundle params = new Bundle();
	 * params.putString("fields", "name,id"); Request request = new
	 * Request(session, requestId, params, HttpMethod.GET, new
	 * Request.Callback() {
	 * 
	 * @Override public void onCompleted(Response response) { if (response !=
	 * null) { GraphObject graphObject = response.getGraphObject(); if
	 * (graphObject != null) { try { jsonArrayGroupMembers =
	 * graphObject.getInnerJSONObject().getJSONArray("data"); for(int i=0;
	 * i<jsonArrayGroupMembers.length(); i++){ JSONObject json; json =
	 * jsonArrayGroupMembers.getJSONObject(i); GroupMember member = new
	 * GroupMember(json.getString("id"), json.getString("name"), null);
	 * guessList.add(member); }
	 * userProfilePic.setImageBitmap(userProfilePicBitmap);
	 * progressDialog.dismiss(); } catch (JSONException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * } } } }); pendingRequest = false; RequestAsyncTask task = new
	 * RequestAsyncTask(request); task.execute(); }
	 * 
	 * 
	 * private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	 * private ProgressDialog progressDialog;
	 * 
	 * @Override protected void onPreExecute() { // TODO Auto-generated method
	 * stub super.onPreExecute(); progressDialog =
	 * ProgressDialog.show(Login.this, "Wait",
	 * "Getting your profile picture..."); }
	 * 
	 * protected Bitmap doInBackground(String... urls) { String profilePicUrl =
	 * urls[0]; Bitmap profilePic = null; try { InputStream in = new
	 * java.net.URL(profilePicUrl).openStream(); profilePic =
	 * BitmapFactory.decodeStream(in); } catch (Exception e) { Log.e("Error",
	 * e.getMessage()); e.printStackTrace(); } return profilePic; }
	 * 
	 * protected void onPostExecute(Bitmap result) { sendGroupMembersRequest();
	 * userProfilePicBitmap = result; progressDialog.dismiss(); } }
	 * 
	 * private Session createSession() { Session activeSession =
	 * Session.getActiveSession(); if (activeSession == null ||
	 * activeSession.getState().isClosed()) {
	 * 
	 * 
	 * activeSession = new
	 * Session.Builder(this).setApplicationId(APP_ID).build();
	 * activeSession.openForRead(new Session.OpenRequest(this)
	 * .setPermissions(Arrays.asList("public_profile","user_groups")));
	 * 
	 * Session.setActiveSession(activeSession); }
	 * 
	 * return activeSession; }
	 * 
	 * private void logoutFromFacebook() { Session session =
	 * Session.getActiveSession(); if (session != null) { if
	 * (!session.isClosed()) { session.closeAndClearTokenInformation(); //clear
	 * your preferences if saved } } else {
	 * 
	 * session = new Session(this); Session.setActiveSession(session);
	 * 
	 * session.closeAndClearTokenInformation(); //clear your preferences if
	 * saved
	 * 
	 * } isLoggedIn = false; updateUI(); }
	 */
	private void initUI() {

		userProfilePic = (ParseImageView) findViewById(R.id.userProfilePic);
		// userProfilePictureView =
		// (ProfilePictureView)findViewById(R.id.userProfilePicture);

		loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setVisibility(View.GONE);
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mConnectionDetector.isConnectingToInternet()) {
					if (!isInitializedParse) {
						initializeParse();
					}
					onLoginButtonClicked();
				} else {
					ToastMessage.showMessage(Login.this,
							ToastMessage.NO_INTERNET_CONNECTION);
				}
			}
		});

		logoutButton = (Button) findViewById(R.id.logout_button);
		logoutButton.setVisibility(View.GONE);
		logoutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				onLogoutButtonClicked();
				// logoutFromFacebook();
			}
		});

		startButton = (Button) findViewById(R.id.start_button);
		startButton.setVisibility(View.GONE);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mConnectionDetector.isConnectingToInternet()) {
					Intent intent = new Intent(Login.this, Gameplay.class);
					intent.putExtra("currentParseUserFacebookId",
							currentParseUserFacebookId);
					startActivity(intent);
				} else {
					ToastMessage.showMessage(Login.this,
							ToastMessage.NO_INTERNET_CONNECTION);
				}
			}
		});

		parseButton = (Button) findViewById(R.id.updateParse_button);
		parseButton.setVisibility(View.GONE);
		parseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mConnectionDetector.isConnectingToInternet()) {
					
					makeGroupRequest();
				} else {
					ToastMessage.showMessage(Login.this,
							ToastMessage.NO_INTERNET_CONNECTION);
				}
			}

		});

		takePhotoButton = (Button) findViewById(R.id.takePhoto_button);
		takePhotoButton.setVisibility(View.GONE);
		takePhotoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

			}
		});

		uploadPhotoButton = (Button) findViewById(R.id.uploadPhoto_button);
		uploadPhotoButton.setVisibility(View.GONE);
		uploadPhotoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				openGallery();
			}
		});

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE_NAME);
		} else {
			mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}

	}

	private void updateUI() {
		if (isLoggedIn) {
			loginButton.setVisibility(View.GONE);
			userProfilePic.setVisibility(View.VISIBLE);
			logoutButton.setVisibility(View.VISIBLE);
			startButton.setVisibility(View.VISIBLE);
			takePhotoButton.setVisibility(View.VISIBLE);
			uploadPhotoButton.setVisibility(View.VISIBLE);
			if (isAdmin)
				parseButton.setVisibility(View.VISIBLE);

		} else {

			loginButton.setVisibility(View.VISIBLE);
			userProfilePic.setVisibility(View.GONE);
			logoutButton.setVisibility(View.GONE);
			startButton.setVisibility(View.GONE);
			parseButton.setVisibility(View.GONE);
			takePhotoButton.setVisibility(View.GONE);
			uploadPhotoButton.setVisibility(View.GONE);
		}

	}

	private class UpdateGroupMember extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			for (int i = 0; i < jsonArrayGroupMembers.length(); i++) {
				if (!isUpdatingParse)
					break;
				try {
					JSONObject json = jsonArrayGroupMembers.getJSONObject(4);
					addGroupMemberToParse(json);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return null;
		}

	}

	private void addGroupMemberToParse(JSONObject json) {
		try {
			tempFacebookId = json.getString("id");
			tempFacebookName = json.getString("name");
			tempFacebookProfilePicUrl = json.getJSONObject("picture")
					.getJSONObject("data").getString("url");
			Log.e("addGroupMemberToParse", Thread.currentThread().getName());

			// Sleep to delay Parse requests
			Thread.sleep(500);

			ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember");
			query.whereEqualTo("facebookId", tempFacebookId);

			ParseObject obj = query.getFirst();
			// If no record is found, go to exception and add that member

			// If found, break the operation
			isUpdatingParse = false;
			progressDialog.dismiss();
			Log.e("Break", Thread.currentThread().getName());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			GroupMember member = new GroupMember();
			member.put("facebookId", tempFacebookId);
			member.put("name", tempFacebookName);
			member.put("facebookProfilePicUrl", tempFacebookProfilePicUrl);
			member.put("usingCustomedName", false);
			member.put("usingCustomedProfilePic", false);
			member.saveInBackground();
			Log.e("Add", tempFacebookName);

		}
	}

	private void openGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
	}

	private void startCropImage() {

		Intent intent = new Intent(this, CropImage.class);
		intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
		intent.putExtra(CropImage.SCALE, true);

		intent.putExtra(CropImage.ASPECT_X, 1);
		intent.putExtra(CropImage.ASPECT_Y, 1);

		startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}

	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

}
