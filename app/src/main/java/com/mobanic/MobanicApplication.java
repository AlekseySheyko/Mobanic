package com.mobanic;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;

public class MobanicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "asQfJUeEXdJnbfvG2iefkvjhVpcmIq2GnOp5x5P2", "rsHyNBGIAmo7eFIUfmjvAm5kOMrwlMCDz7d5El6Z");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}