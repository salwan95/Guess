package com.example.guess;

import java.io.InputStream;
import java.util.List;

import com.fedorvlasov.lazylist.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

public class PopularBaseAdapter extends BaseAdapter {
	private final List<ParseObject> leaderList;
	private LayoutInflater inflater;
	private final Context context;

	private ImageLoader imageLoader; 

	
	
	public PopularBaseAdapter(Context context, List<ParseObject> resultList){
		this.leaderList = resultList;
		this.context = context;
		this.inflater = LayoutInflater.from(this.context);
		this.imageLoader=new ImageLoader(this.context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return leaderList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return leaderList.get(position);
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
			convertView = inflater.inflate(R.layout.popular, parent, false);
			
			mViewHolder = new ViewHolder();
			
			//Picture
			mViewHolder.picture = (ParseImageView) convertView.findViewById(R.id.picture);
			
			
			//Name
			mViewHolder.name = (TextView) convertView.findViewById(R.id.name);
			
			//High Score
			mViewHolder.numGuessedRight = (TextView) convertView.findViewById(R.id.numGuessedRight);
			
			

			convertView.setTag(mViewHolder);

		//} else {
		//	mViewHolder = (ViewHolder) convertView.getTag();			
		//}
		
		mViewHolder.name.setText(leaderList.get(position).getString("name"));
		mViewHolder.numGuessedRight.setText(leaderList.get(position).getInt("numGuessedRight")+"");
		
		String url;
		if(leaderList.get(position).getBoolean("usingCustomedProfilePic")){
			ParseFile file = leaderList.get(position).getParseFile("customedProfilePic");
			if (file == null)
				url = null;
			else
				url = leaderList.get(position).getParseFile("customedProfilePic").getUrl();
		}else
			url = leaderList.get(position).getString("facebookProfilePicUrl");
		
		imageLoader.DisplayImage(url, mViewHolder.picture);
		//new DownloadImageTask(mViewHolder, convertView, position).execute(url);
		
		
		return convertView;
	}


	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
		private ViewHolder mViewHolder;
		private View mView;
		private int position;
		
		public DownloadImageTask(ViewHolder mViewHolder, View mView, int position){
			this.mViewHolder= mViewHolder;
			this.mView = mView;
			this.position =position;
		}

		@Override
		protected Bitmap doInBackground(String...params) {
        
            Bitmap profilePic = null;
            try {
            	
                InputStream in = new java.net.URL(params[0]).openStream();
                profilePic = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return profilePic;
        }

        protected void onPostExecute(Bitmap result) {
        	super.onPostExecute(result);
        	
        	
        	mViewHolder.picture.setImageBitmap(result);
			
        	
        }
		
		
	}

	
	private class ViewHolder{
		
		ParseImageView picture;
		TextView name, numGuessedRight;

	}

}
