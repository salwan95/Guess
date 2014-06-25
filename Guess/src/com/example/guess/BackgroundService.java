package com.example.guess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {
	final String TAG = "Service";
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 @Override
    public void onCreate() {
 
        Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();
        
        Log.d(TAG, "onCreate");
    }
 
    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.e("Service",Thread.currentThread().getName());
        Log.d(TAG, "onStart");    
    }
 
    @Override
    public void onDestroy() {
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }
    

}
