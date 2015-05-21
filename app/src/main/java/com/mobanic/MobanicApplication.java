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
        Parse.initialize(this, "CA2CrEP0v1V4NgRciMU0nh9gJiJLgGHgeq7PWiRT", "u2OdF2BdvIH3uGZhM0VdN09WSLkyiUJy7B4NDhWF");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}