package com.example.guess;

import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentResult extends Fragment {
	private ResultBaseAdapter resultBaseAdapter;
	private ListView listViewResult;
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);
        
        listViewResult =  (ListView) rootView.findViewById(R.id.resultList);
        listViewResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				ParseObject member = (ParseObject) parent.getAdapter().getItem(position);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"+member.getString("facebookId")));
				startActivity(intent);
				
			}
		});
        updateResult();
         
        return rootView;
    }
	
	private void updateResult(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GuessResult").orderByDescending("id");
		
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> parseObjects, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					for(ParseObject obj : parseObjects){
						Log.e("Local", obj.getString("name")+" "+obj.getInt("id"));
					}
					resultBaseAdapter = new ResultBaseAdapter(getActivity(), parseObjects);
					listViewResult.setAdapter(resultBaseAdapter);
				}
				else{
					
				}		
			}
		});		
		
	}
}
