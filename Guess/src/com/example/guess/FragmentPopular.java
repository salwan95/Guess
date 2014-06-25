package com.example.guess;

import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentPopular extends Fragment {
	private PopularBaseAdapter popularBaseAdapter;
	private ListView listViewPopular;
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_popular, container, false);
        
        listViewPopular =  (ListView) rootView.findViewById(R.id.popularList);
        updatePopular();
         
        return rootView;
    }
	
	private void updatePopular(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember").orderByDescending("numGuessedRight");
		query.setLimit(10);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> parseObjects, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					
					popularBaseAdapter = new PopularBaseAdapter(getActivity(), parseObjects);
					listViewPopular.setAdapter(popularBaseAdapter);
				}
				else{
					
				}		
			}
		});		
		
	}
}
