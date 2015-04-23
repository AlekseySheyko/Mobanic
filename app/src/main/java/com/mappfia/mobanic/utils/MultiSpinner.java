package com.mappfia.mobanic.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultiSpinner extends Spinner
        implements OnMultiChoiceClickListener {

    private SearchFiltersListener mListener;
    private ArrayAdapter<String> mAdapter;
    private Set<String> mAllItemsList;
    private boolean[] mCheckboxes;
    private String mFilterKey;

    public MultiSpinner(Context context, AttributeSet attrSet) {
        super(context, attrSet);

        mAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new ArrayList<String>());
        setAdapter(mAdapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
        mCheckboxes[position] = isChecked;
    }

    public void updateSelectedItems() {
        String selectedValue = null;

        Set<String> selectedItemsList = new HashSet<>();
        for (int i = 0; i < mCheckboxes.length; i++) {
            if (mCheckboxes[i]) {
                selectedValue = mAdapter.getItem(i + 1);
                selectedItemsList.add(selectedValue);
            }
        }

        String spinnerText;
        if (selectedItemsList.size() == 0) {
            spinnerText = mFilterKey;
        } else if (selectedItemsList.size() == 1) {
            spinnerText = selectedValue;
        } else {
            if (!mFilterKey.contains("Trans")) {
                spinnerText = selectedItemsList.size() + " " + mFilterKey.toLowerCase() + "s";
            } else {
                spinnerText = selectedItemsList.size() + " trans. types";
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{spinnerText});
        setAdapter(adapter);
        mListener.onFilterSet(mFilterKey, selectedItemsList);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                mAllItemsList.toArray(new CharSequence[mAllItemsList.size()]),
                mCheckboxes,
                this);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateSelectedItems();
            }
        });
        builder.show();
        return true;
    }

    public void setItems(SearchFiltersListener listener, String filterKey, Set<String> allItemsList) {
        mAllItemsList = allItemsList;
        mListener = listener;
        mCheckboxes = new boolean[allItemsList.size()];
        mFilterKey = filterKey;

        mAdapter.add(filterKey);
        mAdapter.addAll(allItemsList);
    }

    public interface SearchFiltersListener {
        void onFilterSet(String filterKey, Set<String> selectedItems);
    }
}