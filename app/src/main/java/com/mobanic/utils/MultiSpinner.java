package com.mobanic.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultiSpinner extends Spinner
        implements OnMultiChoiceClickListener {

    private SearchFiltersListener mListener;
    private ArrayAdapter<String> mAdapter;
    private Set<String> mChoicesList;
    private boolean[] mCheckboxes;
    private String mSearchKey;

    public MultiSpinner(Context context, AttributeSet attrSet) {
        super(context, attrSet);

        mListener = (SearchFiltersListener) context;

        mAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<String>());

        setAdapter(mAdapter);
    }

    public void setItems(String filterKey, Set<String> choicesList) {
        mChoicesList = choicesList;
        mCheckboxes = new boolean[choicesList.size()];
        mSearchKey = filterKey;

        mAdapter.clear();
        mAdapter.addAll(choicesList);
        mAdapter.add(filterKey);
    }

    public void refresh() {
        mAdapter.clear();
        mAdapter.addAll(mChoicesList);
        mAdapter.add(mSearchKey);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
        mCheckboxes[position] = isChecked;
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
        mAdapter.addAll(mChoicesList);
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
        setSelection(mAdapter.getCount());

        mListener.onFilterSet(mSearchKey, selectedItems);
    }

    @Override
    public boolean performClick() {
        if (mChoicesList.size() > 0) {
            CharSequence[] choices = mChoicesList.toArray(
                    new CharSequence[mChoicesList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMultiChoiceItems(choices, mCheckboxes, this);
            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateSelectedItems();
                }
            });
            builder.show();
        } else {
            Toast.makeText(getContext(), "No cars to choose from", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public interface SearchFiltersListener {
        void onFilterSet(String filterKey, Set<String> selectedItems);
    }
}