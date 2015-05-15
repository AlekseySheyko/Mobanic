package com.mobanic.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobanic.R;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener {

    private static final String TAG = MultiSpinner.class.getSimpleName();

    private MultipleFiltersListener mListener;
    private ArrayAdapter<String> mAdapter;
    private Set<String> mChoices;
    private boolean[] mCheckboxes;
    private String mSearchKey;

    public MultiSpinner(Context context, AttributeSet attrs) {
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

        mListener = (MultipleFiltersListener) context;

        mAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>());
        mAdapter.add(mSearchKey);

        setAdapter(mAdapter);
    }

    public void setItems(List<ParseObject> carList) {
        mChoices = new TreeSet<>();
        for (ParseObject car : carList) {
            mChoices.add(car.getString(mSearchKey.toLowerCase().replace("colour", "color")
                    .replace("fuel type", "fuelType").replace("transmission", "transType")));
        }
        mCheckboxes = new boolean[mChoices.size()];

        mAdapter.clear();
        mAdapter.addAll(mChoices);
        mAdapter.add(mSearchKey);
        mAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        setSelection(mAdapter.getCount());
    }

    @Override
    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
        mCheckboxes[position] = isChecked;
    }

    @Override
    public boolean performClick() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> makes = sharedPrefs.getStringSet("Make", null);

        if (mChoices == null || mChoices.size() == 0) {
            Toast.makeText(getContext(), "No cars to choose from", Toast.LENGTH_SHORT).show();
        } else if (!mSearchKey.equals("Make") && (makes == null || makes.size() == 0)) {
            Toast.makeText(getContext(), "Select make first", Toast.LENGTH_SHORT).show();
        } else {
            CharSequence[] choices = mChoices.toArray(
                    new CharSequence[mChoices.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMultiChoiceItems(choices, mCheckboxes, this);
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
        String shownValue = null;

        Set<String> selectedItems = new HashSet<>();
        for (int i = 0; i < mCheckboxes.length; i++) {
            if (mCheckboxes[i]) {
                shownValue = mAdapter.getItem(i);
                selectedItems.add(shownValue);
            }
        }
        mAdapter.clear();
        mAdapter.addAll(mChoices);
        if (selectedItems.size() == 0) {
            mAdapter.add(mSearchKey);
        } else if (selectedItems.size() == 1) {
            mAdapter.add(shownValue);
        } else {
            if (!mSearchKey.contains("Trans")) {
                mAdapter.add(selectedItems.size() + " " + mSearchKey.toLowerCase() + "s");
            } else {
                mAdapter.add(selectedItems.size() + " trans. types");
            }
        }

        mListener.onFilterSet(mSearchKey, selectedItems);
    }

    public interface MultipleFiltersListener {
        void onFilterSet(String filterKey, Set<String> selectedItems);
    }
}