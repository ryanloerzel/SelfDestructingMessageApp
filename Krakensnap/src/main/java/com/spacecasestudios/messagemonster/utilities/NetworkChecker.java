package com.spacecasestudios.messagemonster.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import com.spacecasestudios.messagemonster.R;

/**
 * Created by Ryan on 10/13/2014.
 */
public class NetworkChecker {
    Context myContext;

    public NetworkChecker(Context mContext){
        this.myContext = mContext;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;

    }



    public void closeShop(){
    //Working on way to stop progress if there is no network connectivity but having trouble upon on resume
        //Dialogue to let the user know that a network connection is unavailable
        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setTitle(myContext.getString(R.string.no_network));
        builder.setMessage(myContext.getString(R.string.NoNetworkMessage));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
