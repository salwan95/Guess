package com.example.guess;

import android.content.Context;
import android.widget.Toast;

public class ToastMessage {
	public final static int NO_INTERNET_CONNECTION = 0;
	public static void showMessage(Context context, int messageType){
		switch(messageType){
		case NO_INTERNET_CONNECTION:
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			break;
			
		}
	}
	
}
