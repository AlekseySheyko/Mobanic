package com.mappfia.mobanic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MultiSpinner extends Spinner
        implements OnMultiChoiceClickListener {

    private MakesSpinnerListener mListener;
    private ArrayAdapter<String> mAdapter;
    private List<String> mMakeStrings;
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
        List<String> selectedItemsList = new ArrayList<>();
        for (int i = 0; i < mCheckboxes.length; i++) {
            if (mCheckboxes[i]) {
                selectedItemsList.add(mAdapter.getItem(i + 1));
            }
        }

        String spinnerText;
        if (selectedItemsList.size() == 0) {
            spinnerText = mFilterKey;
        } else if (selectedItemsList.size() == 1) {
            spinnerText = selectedItemsList.get(0);
        } else {
            spinnerText = selectedItemsList.size() + " makes";
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
                mMakeStrings.toArray(new CharSequence[mMakeStrings.size()]),
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

    public void setItems(MakesSpinnerListener listener, String filterKey, List<String> selectedItems) {
        mMakeStrings = selectedItems;
        mListener = listener;
        mCheckboxes = new boolean[selectedItems.size()];
        mFilterKey = filterKey;

        mAdapter.add(filterKey);
        mAdapter.addAll(selectedItems);
    }

    public interface MakesSpinnerListener {
        void onFilterSet(String filterKey, List<String> selectedItems);
    }
}