package com.spacecasestudios.messagemonster;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.spacecasestudios.messagemonster.ui.MainActivity;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

/**
 * Created by Ryan on 6/12/2014.
 */
public class MessageMonsterApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Uql95w4GrXHTygoLB9wQWvISOHY0G8z1BRE4sGQ6", "Iy86VH6dCuUh1WX1OEMp86meu7BcZS6VXn7PgkPi");

        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}
