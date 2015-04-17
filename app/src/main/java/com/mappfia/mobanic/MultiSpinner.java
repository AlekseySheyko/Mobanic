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

public class MultiSpinner extends Spinner implements
        OnMultiChoiceClickListener {

    private boolean[] selected;
    private String defaultText;
    private MultiSpinnerListener mListener;

    private ArrayAdapter<String> mAdapter;
    private List<String> mMakeStrings;

    public MultiSpinner(Context context, AttributeSet attrSet) {
        super(context, attrSet);

        List<String> hintString = new ArrayList<>();
        hintString.add("Make");

        mAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, hintString);
        setAdapter(mAdapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked)
            selected[which] = true;
        else
            selected[which] = false;
    }

    public void updateSelectedItems() {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < mMakeStrings.size(); i++) {
            if (selected[i] == true) {
                spinnerBuffer.append(mMakeStrings.get(i));
                spinnerBuffer.append(", ");
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);
        mListener.onItemsSelected(selected);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                mMakeStrings.toArray(new CharSequence[mMakeStrings.size()]), selected, this);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateSelectedItems();
                    }
                });
        builder.show();
        return true;
    }

    public void setItems(MultiSpinnerListener listener, List<String> makeStrings) {
        mMakeStrings = makeStrings;
        mAdapter.addAll(makeStrings);

        mListener = listener;

        // all selected by default
        selected = new boolean[makeStrings.size()];
        for (int i = 0; i < selected.length; i++)
            selected[i] = true;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
}