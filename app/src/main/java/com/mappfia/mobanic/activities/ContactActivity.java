package com.mappfia.mobanic.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.mappfia.mobanic.R;


public class ContactActivity extends ActionBarActivity {

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private EditText mMessageEditText;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mNameEditText = (EditText) findViewById(R.id.name);
        mEmailEditText = (EditText) findViewById(R.id.email);
        mPhoneEditText = (EditText) findViewById(R.id.phone);
        mMessageEditText = (EditText) findViewById(R.id.message);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Ask Yogz for email to send message to
        if (item.getItemId() == R.id.action_send) {
            Toast.makeText(this, "Thanks, we'll be in touch shortly!", Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.edit()
                .putString("name", mNameEditText.getText().toString())
                .putString("email", mEmailEditText.getText().toString())
                .putString("phone", mPhoneEditText.getText().toString())
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = mSharedPrefs.getString("name", "");
        String email = mSharedPrefs.getString("email", "");
        String phone = mSharedPrefs.getString("phone", "");
        String message = mSharedPrefs.getString("message", "");

        mNameEditText.setText(name);
        mEmailEditText.setText(email);
        mPhoneEditText.setText(phone);

        if (message.isEmpty()) {
            mMessageEditText.requestFocus();
        }
        if (phone.isEmpty()) {
            mPhoneEditText.requestFocus();
        }
        if (email.isEmpty()) {
            mEmailEditText.requestFocus();
        }
        if (name.isEmpty()) {
            mNameEditText.requestFocus();
        }
    }
}
