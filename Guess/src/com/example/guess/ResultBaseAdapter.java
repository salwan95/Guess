package com.example.guess;

import java.io.InputStream;
import java.util.List;










import com.facebook.*;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.fedorvlasov.lazylist.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultBaseAdapter extends BaseAdapter {
	private final List<ParseObject> resultList;
	LayoutInflater inflater;
	private final Context context;
	private ImageLoader imageLoader;

	
	public ResultBaseAdapter(Context context, List<ParseObject> resultList){
		this.resultList = resultList;
		this.context = context;
		inflater = LayoutInflater.from(this.context);
		imageLoader = new ImageLoader(this.context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return resultList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return resultList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder mViewHolder;
		final int finalPosition = position;
		
		//if(convertView == null) {
			convertView = inflater.inflate(R.layout.result, parent, false);
			mViewHolder = new ViewHolder();
			mViewHolder.picture =  (ParseImageView) convertView.findViewById(R.id.picture);
			mViewHolder.name = (TextView) convertView.findViewById(R.id.name);
			mViewHolder.sayHi = (ImageButton) convertView.findViewById(R.id.sayHi);
			mViewHolder.right = (ImageView) convertView.findViewById(R.id.right);
			convertView.setTag(mViewHolder);

		//}
		//else {
		//	mViewHolder = (ViewHolder) convertView.getTag();			
		//
		//	}
		
		ParseObject result = resultList.get(position);
		if(result!=null){
			imageLoader.DisplayImage(result.getString("picUrl"), mViewHolder.picture);
			//new DownloadImageTask(mViewHolder, convertView, position).execute(result.getString("picUrl"));
			
			mViewHolder.name.setText(result.getString("name"));
			
			if(result.getBoolean("isRight"))
				mViewHolder.right.setImageResource(R.drawable.right_sign);
			else
				mViewHolder.right.setImageResource(R.drawable.ic_launcher);
			
			mViewHolder.sayHi.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try{
						sendRequestDialog(resultList.get(finalPosition).getString("facebookId"));
						
						//mViewHolder.intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/messages/"+resultList.get(finalPosition).getString("facebookId")));
						//context.startActivity(mViewHolder.intent);
			        }catch(Exception e){
			        	
			        }
			   }
			});
			
		}

		
		return convertView;
	}
	private void sendRequestDialog(String facebookId) {
	    Bundle params = new Bundle();
	    params.putString("message", "Hi, let's see how well you remember our colleagues' name.");

	    WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(context, ParseFacebookUtils.getSession(), params)).setTo(facebookId)
	            .setOnCompleteListener(new OnCompleteListener() {

	                @Override
	                public void onComplete(Bundle values,
	                    FacebookException error) {
	                    if (error != null) {
	                        if (error instanceof FacebookOperationCanceledException) {
	                            Toast.makeText(context, 
	                                "Request cancelled", 
	                                Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(context, 
	                                "Network Error", 
	                                Toast.LENGTH_SHORT).show();
	                        }
	                    } else {
	                        final String requestId = values.getString("request");
	                        if (requestId != null) {
	                            Toast.makeText(context, 
	                                "Request sent",  
	                                Toast.LENGTH_SHORT).show();
	                        } else {
	                            Toast.makeText(context, 
	                                "Request cancelled", 
	                                Toast.LENGTH_SHORT).show();
	                        }
	                    }   
	                }

	            })
	            .build();
	    requestsDialog.show();
	}
	
	private class ViewHolder{
		ParseImageView picture;
		TextView name;
		ImageView right;
		ImageButton sayHi;
		Intent intent;
	}

}
