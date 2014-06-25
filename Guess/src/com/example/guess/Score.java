package com.example.guess;

import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Score extends FragmentActivity  {
	
	private int score;
	private TextView textViewScore;
	private TextView textViewHighScore;
	
	private ListView listViewLeader;
	private ListView listViewPopular;
	private Button buttonReplay;
	
    private String currentParseUserFacebookId;
    private String currentParseUserFacebookName;
    private String currentParseUserFacebookProfilePicUrl;
    
    private GroupMember currentGroupMember;
    
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    // Tab titles
    private String[] tabs = { "Top Rated", "Games", "Movies" };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);
		
		
		viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        
        viewPager.setAdapter(mAdapter);
		
		
		Intent intent = this.getIntent();
		score = intent.getIntExtra("score", 0);
		
		currentParseUserFacebookId = intent.getStringExtra("currentParseUserFacebookId");
		
		textViewScore = (TextView)findViewById(R.id.textViewScore);
		textViewScore.setText(String.valueOf(score));
		
		textViewHighScore = (TextView)findViewById(R.id.textViewHighScore);
		
		buttonReplay = (Button) findViewById(R.id.buttonReplay);
		buttonReplay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Score.this, Gameplay.class));
			    finish();
			}
		});	
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateHighScore();
	}
	
	private void updateHighScore(){
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember");
		 
		// Retrieve the object by id
		query.whereEqualTo("facebookId", currentParseUserFacebookId);
		query.getFirstInBackground( new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject groupMember, ParseException e) {
				// TODO Auto-generated method stub
				if(groupMember != null){
					currentGroupMember =  new GroupMember(groupMember);
					int highScore = groupMember.getInt("highScore");
					if(highScore< score){
						highScore = score;
						groupMember.put("highScore", highScore);
						groupMember.saveEventually();
					}
					textViewHighScore.setText("High Score: "+highScore);

				}
			}
		});
    }
	
	
	
	@Override
	public void onBackPressed()
	{
	    super.onBackPressed(); 
	    startActivity(new Intent(this, Login.class));
	    finish();

	}
	
	private void onReplay(View view){
		startActivity(new Intent(this, Gameplay.class));
	    finish();
	}
	

	
}
