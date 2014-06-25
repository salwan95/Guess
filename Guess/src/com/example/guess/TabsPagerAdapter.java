package com.example.guess;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
	private static final String[] titles = { "Result", "Leaderboard", "Mr/Miss Popular"};
	
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new FragmentResult();
        case 1:
            // Games fragment activity
            return new FragmentLeader();
        case 2:
            // Movies fragment activity
            return new FragmentPopular();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
    
    
  //...
   
	  @Override
	  public CharSequence getPageTitle(int position) {
	      return titles[position];
	  }
    
 
    

}
