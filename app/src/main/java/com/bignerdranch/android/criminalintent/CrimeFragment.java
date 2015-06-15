package com.bignerdranch.android.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;


public class CrimeFragment extends android.support.v4.app.Fragment {
    public static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";
    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 2;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Callbacks mCallbacks;


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_subject, suspect);
        }
        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }
    public void upDateDate(){
        mDateButton.setText(mCrime.getDate().toString());
    }

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mCrime = new Crime();
        setHasOptionsMenu(true);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK)return;
        if(requestCode == REQUEST_DATE) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mCallbacks.onCrimeUpdated(mCrime);
            upDateDate();
        } else if (requestCode == REQUEST_CONTACT){
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return values for
            String[] queryFields = new String[]{
             ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query- the contactURI is like a "where" clause
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null,null, null);
            // Double check that you actually got results
            if (c.getCount() == 0){
                c.close();
                return;
            }
            // Pull out the first column of the first row of data
            c.moveToFirst();
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mCallbacks.onCrimeUpdated(mCrime);
            mSuspectButton.setText(suspect);
            c.close();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
             if(NavUtils.getParentActivityName(getActivity()) != null){
                 NavUtils.navigateUpFromSameTask(getActivity());
             }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        CrimeLab.get(getActivity()).saveCrimes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, parent, false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            if(NavUtils.getParentActivityName(getActivity()) != null){
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mCrime.setTitle(c.toString());
                mCallbacks.onCrimeUpdated(mCrime);
                getActivity().setTitle(mCrime.getTitle());
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable c) {

            }
        });

        mSuspectButton = (Button)v.findViewById(R.id.crime_suspectButton);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(DateFormat.format("EEEE, MMM dd, yyyy", mCrime.getDate()).toString());
        mDateButton.setEnabled(false);
        upDateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
        Button reportButton = (Button)v.findViewById(R.id.crime_reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });
        Button callButton = (Button)v.findViewById(R.id.crime_callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_CALL, ContactsContract.Contacts.CONTENT_URI);
                startActivity(i);
            }
        });




        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                mCallbacks.onCrimeUpdated(mCrime);
            }
        });

        return v;
    }
}

