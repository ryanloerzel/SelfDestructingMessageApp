package com.spacecasestudios.messagemonster;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Ryan on 6/12/2014.
 */
public class MessageMonsterApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Uql95w4GrXHTygoLB9wQWvISOHY0G8z1BRE4sGQ6", "Iy86VH6dCuUh1WX1OEMp86meu7BcZS6VXn7PgkPi");

    }
}
