package com.mappfia.mobanic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mappfia.mobanic.R;
import com.mappfia.mobanic.utils.Mail;


public class ContactActivity extends ActionBarActivity {

//    private static final String MOBANIC_EMAIL_ADDRESS = "info@mobanic.com";
    private static final String MOBANIC_EMAIL_ADDRESS = "sheykoaaleksey@gmail.com";

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPhoneEditText;
    private EditText mMessageEditText;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mNameEditText = (EditText) findViewById(R.id.name);
        mEmailEditText = (EditText) findViewById(R.id.email);
        mPhoneEditText = (EditText) findViewById(R.id.phone);
        mMessageEditText = (EditText) findViewById(R.id.message);

        mMessageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    private void sendMessage() {
        String name = mNameEditText.getText().toString();
        String emailAddress = mEmailEditText.getText().toString();
        String phone = mPhoneEditText.getText().toString();
        String message = mMessageEditText.getText().toString();
        String subject = "Contact request: " + phone;

        if (name.isEmpty()) {
            showError(mNameEditText);
            return;
        }
        if (emailAddress.isEmpty()) {
            showError(mEmailEditText);
            return;
        }
        if (phone.isEmpty()) {
            showError(mPhoneEditText);
            return;
        }
        if (message.isEmpty()) {
            showError(mMessageEditText);
            return;
        }

        new SendEmailTask().execute(emailAddress, subject, message);
    }

    private class SendEmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String emailAddress = strings[0];
            String subject = strings[1];
            String message = strings[2];

            Mail m = new Mail("mobanic.user@gmail.com", "mobuser123");

            String[] toArr = {MOBANIC_EMAIL_ADDRESS};
            m.setTo(toArr);
            m.setFrom(emailAddress);
            m.setSubject(subject);
            m.setBody(message);

            try {
                if(m.send()) {
                    Toast.makeText(ContactActivity.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ContactActivity.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                }
            } catch(Exception e) {
                Log.e("ContactActivity", "Could not send email", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(ContactActivity.this,
                    "Thanks, we'll be in touch shortly!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ContactActivity.this,
                    MainActivity.class));
        }
    }

    private void showError(EditText editText) {
        Toast.makeText(this, "Enter your " + editText.getHint().toString().toLowerCase(),
                Toast.LENGTH_SHORT).show();
        editText.setError(editText.getHint().toString() + " is required");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            sendMessage();
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
