package com.spacecasestudios.messagemonster.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;
import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.adapter.SectionsPagerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public  static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10MB

    //Create a file path to store photos
    protected Uri mMediaUri;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        }

        else {
            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setTabListener(this));
        }
    }

    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener(){
       @Override
       public void onClick(DialogInterface dialog, int which){
           switch(which){
               case 0: //Take picture
                   Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                   mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                   if(mMediaUri == null){
                       //display error
                       Toast.makeText(MainActivity.this,
                               "There was a problem accessing your device's external storage.",
                               Toast.LENGTH_LONG).show();
                   }
                   else {
                       takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                       startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                   }

                   break;
               case 1: // Take video
                   Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                   mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                   if(mMediaUri == null){
                       //display error
                       Toast.makeText(MainActivity.this,
                               "There was a problem accessing your device's external storage.",
                               Toast.LENGTH_LONG).show();
                   }
                   else {
                       videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                       videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                       videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                       startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                   }
                   break;
               case 2: //Choose Picture
                   Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                   choosePhotoIntent.setType("image/*");
                   startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                   break;
               case 3: //Choose video
                   Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                   chooseVideoIntent.setType("video/*");
                   Toast.makeText(MainActivity.this, getString(R.string.video_file_size_warning), Toast.LENGTH_LONG).show();
                   startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                   break;
           }
       }

        private Uri getOutputMediaFileUri(int mediaType) {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            if(isExternalStorageAvailable()){
                //get Uri
                // Get the external storage directory
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir = new File(
                     Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                     appName);

                //Create sub-directory
                if (! mediaStorageDir.exists()){
                    if( ! mediaStorageDir.mkdirs()){
                        Log.e(TAG, "Failed to create a directory.");
                        return null;
                    }
                }

                //Create file name
                //Create file
                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
                String path = mediaStorageDir.getPath() + File.separator;
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG_" +timestamp + ".jpg");
                }
                else if(mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID" +timestamp + ".mp4");
                }
                else{
                    return null;
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
                return Uri.fromFile(mediaFile);
            }
            else {
                return null;
            }
        }

        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();

            if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }
            else {
                return false;
            }
        }

    };

    @Override
    // Save the picture taken to the devices image gallery
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST){
                if(data == null){
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                }
                else {
                    mMediaUri = data.getData();
                }
                if(requestCode == PICK_VIDEO_REQUEST){
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException e){
                        Toast.makeText(this, getString(R.string.file_not_found_error), Toast.LENGTH_LONG).show();
                        return;
                    } catch (IOException e) {
                        Toast.makeText(this, getString(R.string.file_not_found_error), Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        //Always close inputs streams
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            //This is intentionally blank
                        }
                    }

                    if (fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(this, getString(R.string.error_file_length), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String fileType;
            if(requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST){
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else {
                fileType = ParseConstants.TYPE_VIDEO;
            }
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
            startActivity(recipientsIntent);

        }
        else if (resultCode != RESULT_CANCELED){
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    /* Begin a new intent that takes the user to the login screen
    /  The Main activity is cleared so that the user can not navigate
    /  back unless they are logged in
    */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int itemId = item.getItemId();

        switch(itemId){
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_edit_friends:
                Intent intent = new Intent(this,EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera: //Display a list of camera choices
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog= builder.create();
                dialog.show();
                break;
            case R.id.action_message:
                Intent messageIntent = new Intent(this,CreateMessageActivity.class);
                startActivity(messageIntent);
                break;
            default:
                //TODO do not hard code strings this wayCaravaggio
                Log.i(TAG, "None of the criteria were met");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
}
