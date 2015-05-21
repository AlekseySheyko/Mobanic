package com.mobanic;

import android.app.Application;

import com.mobanic.model.CarParsed;
import com.mobanic.model.CarMobanic;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class MobanicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);
        ParseObject.registerSubclass(CarMobanic.class);
        ParseObject.registerSubclass(CarParsed.class);
        Parse.initialize(this, "asQfJUeEXdJnbfvG2iefkvjhVpcmIq2GnOp5x5P2", "rsHyNBGIAmo7eFIUfmjvAm5kOMrwlMCDz7d5El6Z");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}