package com.example.pedometer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.pedometer.R;
import com.example.pedometer.helper.CustomViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

// https://stackoverflow.com/questions/41413150/fragment-tabs-inside-fragment
// switches between two more fragments
public class HealthFragmentParent extends Fragment  {

    public HealthFragmentParent() {
    // required the empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_health_parent,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();


        // Setting ViewPager for each Tabs
        CustomViewPager customViewPager = (CustomViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(customViewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(customViewPager);

        return view;

    }


    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new GraphWeightDataFragment(), "Weight data");
        adapter.addFragment(new GraphWeightFragment(), "Graph");
        adapter.addFragment(new BmiFragment(), "BMI");

       // adapter.addFragment(new PlaceholderFragment(), "Placeholder loooooooooooooooooooooooooooooooooooooooooooo");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
