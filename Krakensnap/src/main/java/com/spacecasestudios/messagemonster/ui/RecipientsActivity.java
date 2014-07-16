package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.adapter.UserAdapter;
import com.spacecasestudios.messagemonster.utilities.FileHelper;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

import java.util.ArrayList;
import java.util.List;

public class RecipientsActivity extends Activity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;
    protected Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);

        mGridView = (GridView)findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        TextView emptyTextView = (TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        mSendButton = (Button) findViewById(R.id.send_button);
        mSendButton.setVisibility(View.VISIBLE);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGridView.getCheckedItemCount() > 0) {
                    ParseObject message = createMessage();
                    if (message == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                        builder.setMessage(R.string.error_selecting_file)
                                .setTitle(R.string.error_selecting_file_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        send(message);
                        Toast.makeText(RecipientsActivity.this, getString(R.string.uploading_message), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(RecipientsActivity.this, "Please select a recipient", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

       this.setProgressBarIndeterminateVisibility(true);

        //retrieve the query object from the friends relation
        ParseQuery<ParseUser> query =  mFriendsRelation.getQuery();

        //Sort by user name
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> friends, ParseException e) {
               setProgressBarIndeterminateVisibility(false);
                if( e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    if (mGridView.getAdapter() == null){
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else {
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }

                }
                else{
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_send:
                ParseObject message = createMessage();
                if(message == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.error_selecting_file)
                            .setTitle(R.string.error_selecting_file_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    send(message);
                    Toast.makeText(RecipientsActivity.this, getString(R.string.uploading_message), Toast.LENGTH_LONG).show();
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ParseObject createMessage(){
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());

        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        if(fileBytes == null){
            return null;
        }
        else {
            if(mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String filename = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(filename, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }

    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        for(int i = 0; i < mGridView.getCount(); i++){
            if(mGridView.isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }

    protected void send(ParseObject message){
        setProgressBarIndeterminateVisibility(true);
        message.saveInBackground(new SaveCallback() {

            @Override
            public void done(ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e == null){
                    Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
                    sendPushNotification();
                }
                else{
                    Toast.makeText(RecipientsActivity.this, R.string.error_connecting_with_parse, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            if (mGridView.getCheckedItemCount() > 0) {
                mSendMenuItem.setVisible(true);
            }
            else {
                mSendMenuItem.setVisible(false);
            }

            ImageView checkImageView = (ImageView)view.findViewById(R.id.checkImageView);

            if( mGridView.isItemChecked(position)){
                checkImageView.setVisibility(View.VISIBLE);
            }
            else{
                checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    };

    protected void sendPushNotification(){
        ParseQuery query = ParseInstallation.getQuery();

        //Notify users WHERE there ID is in one of the recipient IDs
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        //send push notification
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();

    }
}


