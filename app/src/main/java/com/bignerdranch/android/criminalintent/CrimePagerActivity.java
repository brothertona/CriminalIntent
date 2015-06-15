package com.bignerdranch.android.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.bignerdranch.android.criminalintent.CrimeFragment.Callbacks;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by andrew on 6/8/2015.
 */
public class CrimePagerActivity extends FragmentActivity implements Callbacks {



        private ViewPager mViewPager;
        private ArrayList<Crime> mCrimes;

        @Override
        public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        mCrimes = CrimeLab.get(this).getCrimes();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mCrimes.size();
            }

            @Override
            public Fragment getItem(int pos) {
                Crime crime = mCrimes.get(pos);
                return CrimeFragment.newInstance(crime.getId());
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int pos, float posOffset, int posOffSetPixels) {
            }

            public void onPageSelected(int pos) {
                Crime crime = mCrimes.get(pos);
                if (crime.getTitle() != null) {
                    setTitle(crime.getTitle());
                }
            }
        });


        UUID crimeId = (UUID) getIntent()
                .getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }

        }
    }
    @Override
    public void onCrimeUpdated(Crime crime) {

    }

}
