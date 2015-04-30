package com.mobanic;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

public class MobanicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "asQfJUeEXdJnbfvG2iefkvjhVpcmIq2GnOp5x5P2", "rsHyNBGIAmo7eFIUfmjvAm5kOMrwlMCDz7d5El6Z");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("");
    }
}