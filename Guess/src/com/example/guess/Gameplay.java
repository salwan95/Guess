package com.example.guess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.guess.util.SystemUiHider;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Gameplay extends Activity implements OnClickListener {

	private final String FACEBOOK_PROFILE_PIC_URL = "facebookProfilePicUrl";

	private List<GroupMember> guessList;
	private List<ParseObject> originalGuessList;
	// private List<ParseObject> guessList;
	private Button buttonAnswer1;
	private Button buttonAnswer2;
	private Button buttonAnswer3;
	private Button buttonAnswer4;
	private TextView textViewScore;
	private TextView textViewTimeCount;
	private TextView textViewAnswer;
	private GroupMember tempMember1;
	private GroupMember tempMember2;
	private GroupMember tempMember3;
	private GroupMember tempMember4;

	private ImageView imageViewGuessProfilePic;
	// private ProfilePictureView userProfilePicture;
	private int rightAnswer;
	private int score = 0;
	private int numQuestion = 0;

	private GroupMember currentGroupMember;

	private String currentParseUserFacebookId;
	private String currentParseUserFacebookName;
	private String currentParseUserFacebookProfilePicUrl;
	private Bitmap profilePic = null;
	
	private ConnectionDetector mConnectionDetector;

	private String tempId;
	private String tempName;
	private String tempPicUrl;

	

	private CountDownTimer cdTimer;

	public GroupMember getCurrentGroupMember() {
		return currentGroupMember;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gameplay);
		mConnectionDetector = new ConnectionDetector(getApplicationContext());;

		Intent intent = this.getIntent();
		currentParseUserFacebookId = intent.getStringExtra("currentParseUserFacebookId");
		
		guessList = new ArrayList<GroupMember>();
		ParseObject.unpinAllInBackground();
		
		
		if (mConnectionDetector.isConnectingToInternet()) {
			fetchGroupMemberFromParse();
		} else {
			ToastMessage.showMessage(this,
					ToastMessage.NO_INTERNET_CONNECTION);
		}
		

		textViewTimeCount = (TextView) findViewById(R.id.timeCount);
		textViewScore = (TextView) findViewById(R.id.score);
		textViewAnswer = (TextView) findViewById(R.id.answer);
		
		
		cdTimer = new CountDownTimer(7000, 1000) {
			public void onTick(long millisUntilFinished) {
				textViewTimeCount.setText("Time remaining: "
						+ (millisUntilFinished / 1000 - 1));
			}

			public void onFinish() {
				textViewAnswer.setVisibility(View.VISIBLE);
				textViewAnswer.setText("Time out");
				

			}
		};
		imageViewGuessProfilePic = (ImageView) findViewById(R.id.imageViewProfilePic);
		// userProfilePicture =
		// (ProfilePictureView)findViewById(R.id.userProfilePicture);
		buttonAnswer1 = (Button) findViewById(R.id.buttonAnswer1);
		buttonAnswer2 = (Button) findViewById(R.id.buttonAnswer2);
		buttonAnswer3 = (Button) findViewById(R.id.buttonAnswer3);
		buttonAnswer4 = (Button) findViewById(R.id.buttonAnswer4);
		buttonAnswer1.setOnClickListener(this);
		buttonAnswer2.setOnClickListener(this);
		buttonAnswer3.setOnClickListener(this);
		buttonAnswer4.setOnClickListener(this);
	}

	private void fetchGroupMemberFromParse() {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"GroupMember");
		try {
			// Get all members except currently logged in user
			// parseObjects = query.whereNotEqualTo("facebookId",
			// currentUserFacebookId).find();

			// Get all members
			originalGuessList = query.find();
			for (ParseObject obj : originalGuessList) {
				guessList.add(new GroupMember(obj));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ParseObject.unpinAllInBackground();
		guessList.clear();
		imageViewGuessProfilePic.setVisibility(View.INVISIBLE);
		textViewAnswer.setVisibility(View.INVISIBLE);
		textViewScore.setVisibility(View.INVISIBLE);
		textViewTimeCount.setVisibility(View.INVISIBLE);
		buttonAnswer1.setVisibility(View.INVISIBLE);
		buttonAnswer2.setVisibility(View.INVISIBLE);
		buttonAnswer3.setVisibility(View.INVISIBLE);
		buttonAnswer4.setVisibility(View.INVISIBLE);
		for (ParseObject obj : originalGuessList) {
			guessList.add(new GroupMember(obj));
		}
		if(mConnectionDetector.isConnectingToInternet())
			loadQuestion();
		else
			ToastMessage.showMessage(this, ToastMessage.NO_INTERNET_CONNECTION);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		imageViewGuessProfilePic.setVisibility(View.INVISIBLE);
		textViewAnswer.setVisibility(View.INVISIBLE);
		textViewScore.setVisibility(View.INVISIBLE);
		textViewTimeCount.setVisibility(View.INVISIBLE);
		buttonAnswer1.setVisibility(View.INVISIBLE);
		buttonAnswer2.setVisibility(View.INVISIBLE);
		buttonAnswer3.setVisibility(View.INVISIBLE);
		buttonAnswer4.setVisibility(View.INVISIBLE);
		cdTimer.cancel();
	}
	
	
	@Override
	public void onClick(View v) {
		boolean isRightGuess = false;
		textViewAnswer.setVisibility(View.VISIBLE);
		cdTimer.cancel();

		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonAnswer1:
			if (rightAnswer == 0) {
				isRightGuess = true;
				
			} else {
				isRightGuess = false;
			}
			break;
		case R.id.buttonAnswer2:
			if (rightAnswer == 1) {
				isRightGuess = true;
			} else {
				isRightGuess = false;
			}
			break;
		case R.id.buttonAnswer3:
			if (rightAnswer == 2) {
				isRightGuess = true;
			} else {
				isRightGuess = false;
			}
			break;
		case R.id.buttonAnswer4:
			if (rightAnswer == 3) {
				isRightGuess = true;
			} else {
				isRightGuess = false;
			}
			break;

		}
		//Update score
		
		
		GuessResult result = new GuessResult();
		result.setId(numQuestion);
		result.setFacebookId(tempMember1.getFacebookId());
		result.setName(tempMember1.getName());
		result.setPicUrl(tempMember1.getCurrentProfilePicUrl());
		result.setRight(isRightGuess);
		result.pinInBackground(null);
		updateGropMemberRecord(tempMember1, isRightGuess);
		
		if(isRightGuess){
			textViewAnswer.setText("Correct");
			score++;
			textViewScore.setText("Level " + score);
			loadQuestion();
		}
		else
		{
			textViewAnswer.setText("Wrong");
			
			//Game ends, show result
			Intent intent = new Intent(this, Score.class);
			intent.putExtra("score", score);
			intent.putExtra("currentParseUserFacebookId", currentParseUserFacebookId);
			startActivity(intent);
		}
		
		
		
		
		// UpdateThisMemberRightOrWrongGuessedNumber
		// SaveThisQuestionInfoToPhone
	}
	private void updateGropMemberRecord(GroupMember member, boolean isRightGuess){
		final boolean finalIsRightGuess = isRightGuess;
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember");
		query.whereEqualTo("facebookId", member.getFacebookId());
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject object, ParseException e) {
				// TODO Auto-generated method stub
				if(object!=null){
					object.put("numGuessed", object.getInt("numGuessed")+1);
					if(finalIsRightGuess)
						object.put("numGuessedRight", object.getInt("numGuessedRight")+1);
					else
						object.put("numGuessedWrong", object.getInt("numGuessedWrong")+1);
					object.saveEventually();
				}
				
			}
		});		
	}
	

	private class SaveFileToParse extends AsyncTask<ParseObject, Void, Void> {
		@Override
		protected Void doInBackground(ParseObject... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			loadQuestion();
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int timeCount = msg.getData().getInt("timeCount");
			textViewTimeCount.setText(String.valueOf(timeCount));
		}
	};

	private void loadQuestion() {
		if (guessList.size() == 0) {
			textViewAnswer.setText("You have cleared all");
			return;
		}
		numQuestion++;

		int[] guessMembers = getRandomNumbers();
		if (guessMembers.length == 1) {
			tempMember1 = guessList.get(guessMembers[0]);
		} else if (guessMembers.length == 2) {
			tempMember1 = guessList.get(guessMembers[0]);
			tempMember2 = guessList.get(guessMembers[1]);
		} else if (guessMembers.length == 3) {
			tempMember1 = guessList.get(guessMembers[0]);
			tempMember2 = guessList.get(guessMembers[1]);
			tempMember3 = guessList.get(guessMembers[2]);
		} else {
			tempMember1 = guessList.get(guessMembers[0]);
			tempMember2 = guessList.get(guessMembers[1]);
			tempMember3 = guessList.get(guessMembers[2]);
			tempMember4 = guessList.get(guessMembers[3]);
		}

		tempId = tempMember1.getFacebookId();
		tempName = tempMember1.getName();
		if (tempMember1.getUsingCustomedProfilePic()) {
			tempPicUrl = tempMember1.getCustomedProfilePic().getUrl();
			new DownloadImageTask().execute(tempPicUrl);
		} else {
			getLatestFacebookProfilePic(tempMember1);

		}

		guessList.remove(guessMembers[0]);
	}

	private void getLatestFacebookProfilePic(GroupMember tempMember1) {
		final String savedFacebookProfilePicUrl = tempMember1
				.getFacebookProfilePicUrl();
		final String id = tempMember1.getFacebookId();
		String requestId = tempMember1.getFacebookId() + "/picture";
		Bundle params = new Bundle();
		params.putString("width", "400");
		params.putString("height", "400");
		params.putString("redirect", "false");
		Request request = new Request(ParseFacebookUtils.getSession(),
				requestId, params, HttpMethod.GET, new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						if (response != null) {
							GraphObject graphObject = response.getGraphObject();
							if (graphObject != null) {
								try {
									JSONObject json = graphObject
											.getInnerJSONObject();
									final String currentFacebookProfilePicUrl = json
											.getJSONObject("data").getString(
													"url");
									if (currentFacebookProfilePicUrl
											.equals(savedFacebookProfilePicUrl)) {
										tempPicUrl = savedFacebookProfilePicUrl;
										new DownloadImageTask()
												.execute(savedFacebookProfilePicUrl);
									} else {
										tempPicUrl = currentFacebookProfilePicUrl;
										new DownloadImageTask()
												.execute(currentFacebookProfilePicUrl);
										ParseQuery<ParseObject> query = ParseQuery
												.getQuery("GroupMember");

										// Retrieve the object by id
										query.whereEqualTo("facebookId", id);
										query.getFirstInBackground(new GetCallback<ParseObject>() {
											@Override
											public void done(
													ParseObject groupMember,
													ParseException e) {
												// TODO Auto-generated method
												// stub
												groupMember.put("facebookProfilePicUrl", currentFacebookProfilePicUrl);
												groupMember.saveInBackground();
											}
										});
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}

					}
				});
		request.executeAsync();
	}

	private int[] getRandomNumbers() {
		int guessListSize = guessList.size();

		if (guessListSize == 1) {
			int[] randomNumbers = new int[1];
			randomNumbers[0] = 0;
			return randomNumbers;
		} else if (guessListSize == 2) {
			int[] randomNumbers = new int[2];
			randomNumbers[0] = (int) (guessListSize * Math.random());
			randomNumbers[1] = 1 - randomNumbers[0];
			return randomNumbers;
		} else if (guessListSize == 3) {
			int[] randomNumbers = new int[3];
			randomNumbers[0] = (int) (guessListSize * Math.random());
			do {
				randomNumbers[1] = (int) (guessListSize * Math.random());
			} while (randomNumbers[1] == randomNumbers[0]);
			do {
				randomNumbers[2] = (int) (guessListSize * Math.random());
			} while (randomNumbers[2] == randomNumbers[0]
					|| randomNumbers[2] == randomNumbers[1]);
			return randomNumbers;
		}

		else {
			int[] randomNumbers = new int[4];
			randomNumbers[0] = (int) (guessListSize * Math.random());
			do {
				randomNumbers[1] = (int) (guessListSize * Math.random());
			} while (randomNumbers[1] == randomNumbers[0]);
			do {
				randomNumbers[2] = (int) (guessListSize * Math.random());
			} while (randomNumbers[2] == randomNumbers[0]
					|| randomNumbers[2] == randomNumbers[1]);
			do {
				randomNumbers[3] = (int) (guessListSize * Math.random());
			} while (randomNumbers[3] == randomNumbers[0]
					|| randomNumbers[3] == randomNumbers[1]
					|| randomNumbers[3] == randomNumbers[2]);
			return randomNumbers;
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		protected Bitmap doInBackground(String... urls) {
			String profilePicUrl = urls[0];
			
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
			textViewScore.setText("Level " + score);
			imageViewGuessProfilePic.setImageBitmap(result);
			imageViewGuessProfilePic.setVisibility(View.VISIBLE);
			imageViewGuessProfilePic.setVisibility(View.INVISIBLE);
			
			textViewScore.setVisibility(View.VISIBLE);
			textViewTimeCount.setVisibility(View.VISIBLE);
			
			buttonAnswer1.setVisibility(View.VISIBLE);
			buttonAnswer2.setVisibility(View.VISIBLE);
			buttonAnswer3.setVisibility(View.VISIBLE);
			buttonAnswer4.setVisibility(View.VISIBLE);
			textViewAnswer.setVisibility(View.GONE);

			int guessListSize = guessList.size();
			if (guessListSize == 0) {
				buttonAnswer1.setText(tempMember1.getName());
				buttonAnswer2.setVisibility(View.INVISIBLE);
				buttonAnswer3.setVisibility(View.INVISIBLE);
				buttonAnswer4.setVisibility(View.INVISIBLE);
			} else if (guessListSize == 1) {
				rightAnswer = (int) ((guessListSize + 1) * Math.random());
				switch (rightAnswer) {
				case 0:
					buttonAnswer1.setText(tempMember1.getName());
					buttonAnswer2.setText(tempMember2.getName());
					break;
				case 1:
					buttonAnswer1.setText(tempMember2.getName());
					buttonAnswer2.setText(tempMember1.getName());
					break;
				}
				buttonAnswer3.setVisibility(View.INVISIBLE);
				buttonAnswer4.setVisibility(View.INVISIBLE);
			} else if (guessList.size() == 2) {
				rightAnswer = (int) ((guessListSize + 1) * Math.random());
				switch (rightAnswer) {
				case 0:
					buttonAnswer1.setText(tempMember1.getName());
					buttonAnswer2.setText(tempMember2.getName());
					buttonAnswer3.setText(tempMember3.getName());
					break;
				case 1:
					buttonAnswer1.setText(tempMember2.getName());
					buttonAnswer2.setText(tempMember1.getName());
					buttonAnswer3.setText(tempMember3.getName());
					break;
				case 2:
					buttonAnswer1.setText(tempMember3.getName());
					buttonAnswer2.setText(tempMember2.getName());
					buttonAnswer3.setText(tempMember1.getName());
				}
				buttonAnswer4.setVisibility(View.INVISIBLE);
			} else {
				rightAnswer = (int) (4 * Math.random());
				switch (rightAnswer) {
				case 0:
					buttonAnswer1.setText(tempMember1.getName());
					buttonAnswer2.setText(tempMember2.getName());
					buttonAnswer3.setText(tempMember3.getName());
					buttonAnswer4.setText(tempMember4.getName());
					break;
				case 1:
					buttonAnswer1.setText(tempMember2.getName());
					buttonAnswer2.setText(tempMember1.getName());
					buttonAnswer3.setText(tempMember3.getName());
					buttonAnswer4.setText(tempMember4.getName());
					break;
				case 2:
					buttonAnswer1.setText(tempMember3.getName());
					buttonAnswer2.setText(tempMember2.getName());
					buttonAnswer3.setText(tempMember1.getName());
					buttonAnswer4.setText(tempMember4.getName());
					break;
				case 3:
					buttonAnswer1.setText(tempMember4.getName());
					buttonAnswer2.setText(tempMember2.getName());
					buttonAnswer3.setText(tempMember3.getName());
					buttonAnswer4.setText(tempMember1.getName());
					break;
				}
				imageViewGuessProfilePic.setVisibility(View.VISIBLE);
				buttonAnswer1.setVisibility(View.VISIBLE);
				buttonAnswer2.setVisibility(View.VISIBLE);
				buttonAnswer3.setVisibility(View.VISIBLE);
				buttonAnswer4.setVisibility(View.VISIBLE);
			}

			cdTimer.start();
		}
	}
	@Override
	public void onBackPressed()
	{
	    super.onBackPressed(); 
	    startActivity(new Intent(this, Login.class));
	    finish();

	}
	

}
