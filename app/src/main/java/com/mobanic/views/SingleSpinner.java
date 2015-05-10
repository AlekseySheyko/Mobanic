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

import com.mobanic.Car;
import com.mobanic.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SingleSpinner extends Spinner implements DialogInterface.OnMultiChoiceClickListener {

    private AgeFilterListener mListener;
    private ArrayAdapter<String> mAdapter;
    private Set<String> mAgeCategoriesList;
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

    public void setItems(List<Car> carList) {
        mAgeCategoriesList = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return extractDigits(s) - extractDigits(t1);
            }
        });
        for (Car car : carList) {
            mAgeCategoriesList.add(car.getAgeCategory());
        }

        mAdapter.clear();
        mAdapter.addAll(mAgeCategoriesList);
        mAdapter.add(mSearchKey);
        mAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        setSelection(mAdapter.getCount());
    }

    private int extractDigits(String s) {
        return Integer.parseInt(s.replaceAll("\\D+",""));
    }

    @Override
    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
    }

    @Override
    public boolean performClick() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> makes = sharedPrefs.getStringSet("Make", null);

        if (mAgeCategoriesList.size() == 0) {
            Toast.makeText(getContext(), "No cars to choose from", Toast.LENGTH_SHORT).show();
        } else if (makes == null || makes.size() == 0) {
            Toast.makeText(getContext(), "Select make first", Toast.LENGTH_SHORT).show();
        } else {
            CharSequence[] choices = mAgeCategoriesList.toArray(
                    new CharSequence[mAgeCategoriesList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setSingleChoiceItems(choices, mSelectedValue, this);
            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateSelectedItems();
                }
            });
            builder.show();
        }
        return true;
    }

    private void updateSelectedItems() {
        mAdapter.clear();
        mAdapter.addAll(mAgeCategoriesList);
        if (mSelectedValue == 0) {
            mAdapter.add(mSearchKey);
        } else {
            mAdapter.add(mSelectedValue + "");
        }

        mListener.onFilterSet(mSearchKey, (Set<String>) Arrays.asList(mSelectedValue + ""));
    }

    public interface AgeFilterListener {
        void onFilterSet(String filterKey, Set<String> selectedItems);
    }
}
