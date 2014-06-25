package com.example.guess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("GroupMember")

public class GroupMember extends ParseObject{
	public GroupMember(){

	}
	public GroupMember(ParseObject obj){
		put("facebookId", obj.getString("facebookId"));
		put("name", obj.getString("name"));	
		put("createdAt", obj.getCreatedAt());
		put("updatedAt", obj.getUpdatedAt());
		
		if(obj.getString("customedName")!=null)
			put("customedName", obj.getString("customedName"));
		
		put("usingCustomedProfilePic", obj.getBoolean("usingCustomedProfilePic"));
		put("usingCustomedName", obj.getBoolean("usingCustomedName"));
		put("facebookProfilePicUrl", obj.getString("facebookProfilePicUrl"));
		
		ParseFile file = obj.getParseFile("customedProfilePic");
		if (file !=null)
			put("customedProfilePic", file);
		
	}
	public GroupMember(String id, String name, String facebookProfilePicUrl){
		put("facebookId", id);
		put("name", name);
		put("facebookProfilePicUrl", facebookProfilePicUrl);
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
	
	public String getFacebookProfilePicUrl(){
		return getString("facebookProfilePicUrl");
	}
	public void setFacebookProfilePicUrl(String profilePicUrl){
		put("facebookProfilePicUrl", profilePicUrl);
	}
	
	public boolean getUsingCustomedProfilePic(){
		return getBoolean("usingCustomedProfilePic");
	}
	public void settUsingCustomedProfilePic(boolean usingCustomedProfilePic){
		put("usingCustomedProfilePic", usingCustomedProfilePic);
	}
	

	public boolean getUsingCustomedName(){
		return getBoolean("usingCustomedName");
	}
	public void settUsingCustomedName(boolean usingCustomedName){
		put("usingCustomedName", usingCustomedName);
	}
	
	
	public ParseFile getCustomedProfilePic() {
		return getParseFile("customedProfilePic");
	}
	public void setCustomedProfilePic(ParseFile customedProfilePic) {
		put("customedProfilePic", customedProfilePic);
	}
	
	
	public String getCustomedProfilePicUrl(){
		ParseFile file = getCustomedProfilePic();
		if(file!=null)
			return file.getUrl();
		else
			return null;
	}
	
	public String getCurrentProfilePicUrl(){
		if(getUsingCustomedProfilePic()){
			return getCustomedProfilePicUrl();
		}
		else{
			return getFacebookProfilePicUrl();
		}
	}
	
	
	public ParseFile getParseFileFromBitmap(Bitmap bm){
		  
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	    // get byte array here
	    byte[] bytearray= stream.toByteArray();
	    ParseFile pf = new ParseFile("parseFileImage.jpg",bytearray);
	    return pf;
	}
	
	public Bitmap getBitMapFromParseFile(ParseFile pf){
		byte[] file;
		try {
			file = pf.getData();
			Bitmap bm = BitmapFactory.decodeByteArray(file,0,file.length);
			return bm;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
