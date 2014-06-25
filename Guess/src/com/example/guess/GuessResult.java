package com.example.guess;

import android.graphics.Bitmap;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;


@ParseClassName("GuessResult")

public class GuessResult extends ParseObject{

	public GuessResult(){

	}
	
	public int getId(){
		return getInt("id");
	}
	public void setId(int id){
		put("id", id);
	}
	
	public String getFacebookId(){
		return getString("facebookId");
	}
	public void setFacebookId(String facebookId){
		put("facebookId", facebookId);
	}
	
	public String getName(){
		return getString("name");
	}
	public void setName(String name){
		put("name", name);
	}
	public String getPicUrl(){
		return getString("picUrl");
	}
	public void setPicUrl(String picUrl){
		put("picUrl", picUrl);
	}
	

	public boolean isRight(){
		return getBoolean("isRight");
	}
	public void setRight(boolean isRight){
		put("isRight", isRight);
	}
	
	public ParseFile getCustomedProfilePic() {
		return getParseFile("customedProfilePic");
	}
	
	public ParseFile getCurrentProfilePic() {
		return getParseFile("currentProfilePic");
	}
	public void setCurrentPofilePic(ParseFile currentProfilePic){
		put("currentProfilePic", currentProfilePic);
	}
}
