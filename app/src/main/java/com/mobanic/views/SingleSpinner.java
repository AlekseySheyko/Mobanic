package com.mobanic.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobanic.CarFromMobanic;
import com.mobanic.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SingleSpinner extends Spinner {

    private AgeFilterListener mListener;
    private ArrayAdapter<String> mAdapter;
    private Set<String> mAgeSet;
    private ArrayList<String> mAgeList;
    private int mSelectedValue;
    private String mSearchKey;

    public SingleSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SearchSpinner,
                0, 0);

        try {
            mSearchKey = a.getString(R.styleable.SearchSpinner_searchKey);
        } finally {
            a.recycle();
        }

        mListener = (AgeFilterListener) context;

        mAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>());
        mAdapter.add(mSearchKey);

        setAdapter(mAdapter);
    }

    public void setItems(List<CarFromMobanic> carList) {
        mAgeSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return extractDigits(s) - extractDigits(t1);
            }
        });
        for (CarFromMobanic car : carList) {
            mAgeSet.add(car.getAgeCategory());
        }
        mAgeList = new ArrayList<>();
        for (String age : mAgeSet) {
            mAgeList.add(age);
        }

        mAdapter.clear();
        mAdapter.addAll(mAgeSet);
        mAdapter.add(mSearchKey);
        mAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        setSelection(mAdapter.getCount());

        mSelectedValue = mAgeSet.size();
    }

    @Override
    public boolean performClick() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> makes = sharedPrefs.getStringSet("Make", null);

        if (mAgeSet == null || mAgeSet.size() == 0) {
            Toast.makeText(getContext(), "No cars to choose from", Toast.LENGTH_SHORT).show();
        } else if (makes == null || makes.size() == 0) {
            Toast.makeText(getContext(), "Select make first", Toast.LENGTH_SHORT).show();
        } else {
            CharSequence[] choices = mAgeSet.toArray(
                    new CharSequence[mAgeSet.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setSingleChoiceItems(choices, mSelectedValue, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    mSelectedValue = position;
                }
            });
            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    updateSelectedItems();
                }
            });
            builder.show();
        }
        return true;
    }

    private void updateSelectedItems() {
        String ageStr = mAgeList.get(mSelectedValue);

        mAdapter.clear();
        mAdapter.add(ageStr);

        mListener.onAgeSelected(extractDigits(ageStr));
    }

    public interface AgeFilterListener {
        void onAgeSelected(int maxAge);
    }

    private int extractDigits(String s) {
        return Integer.parseInt(s.replaceAll("\\D+", ""));
    }
}
