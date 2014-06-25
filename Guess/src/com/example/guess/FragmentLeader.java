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

public class FragmentLeader extends Fragment {
	private LeaderBaseAdapter leaderBaseAdapter;
	private ListView listViewLeader;
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_leader, container, false);
        
        listViewLeader =  (ListView) rootView.findViewById(R.id.leaderList);
        updateLeaderBoard();
         
        return rootView;
    }
	
	private void updateLeaderBoard(){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMember").orderByDescending("highScore");
		query.setLimit(10);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> parseObjects, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					
					leaderBaseAdapter = new LeaderBaseAdapter(getActivity(), parseObjects);
					listViewLeader.setAdapter(leaderBaseAdapter);
				}
				else{
					
				}		
			}
		});		
		
	}
}
