/**************************************************************************************************
 * File EditFriendsActivity.java
 * Author: Ryan Loerzel
 * Created: July 16, 2014
 * Description: Add and remove contacts
 **************************************************************************************************/
package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.adapter.UserAdapter;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

import java.util.List;


public class EditFriendsActivity extends Activity {

    //Declare member variables
    public static final String TAG = EditFriendsActivity.class.getSimpleName();
    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;
    protected TextView mContactEmail;
    protected Button mValidateEmail;
    protected String mEmail;
    protected LinearLayout mLayout;
   protected ImageView mCheckImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid_edit_friends);

        //Initialize member variables
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        mLayout = (LinearLayout) findViewById(R.id.linear_layout);
        mContactEmail = (TextView) findViewById(R.id.contactEmail);
        mValidateEmail = (Button) findViewById(R.id.validateButton);

    }

    @Override
    protected void onResume() {
        /**
         * Retrieve a List of of ParseUser Objects from parse.com, sorted by username.
         * Set the progress bar to visible while loading and invisible when finished
         * Assign the ParseUser List to the mUsers variable and create an array of user names
         * Set the Grid view to hold the Parse user names
         */
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();

        //*********************************************************************************
        //A null pointer exception was being thrown without this try-catch block
        //TODO: figure out what is causing the exception
        try {
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        //***********************************************************************************

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        setProgressBarIndeterminateVisibility(true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    /**If e == null then the query was successful and Parse Users were returned*/
                    mUsers = parseUsers;
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                    addFriendCheckmarks();


                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        /**Handle action bar item clicks here. The action bar will
         *  automatically handle clicks on the Home/Up button, so long
         *  as you specify a parent activity in AndroidManifest.xml.
         */
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    private void addFriendCheckmarks() {
    /**
     * If a parse user is one of the current users contacts,
     * then a check mark will be displayed on that parse users icon.
     */
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    //list returned look for match
                    for (int i = 0; i < mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : friends) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        /**
         * Prompt user for email verification when adding a contact.
         * Remove check mark and relationship when deleting a contact.
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            mCheckImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {

               checkEmail(position);

            } else {
                //remove
                mFriendsRelation.remove(mUsers.get(position));
                mCheckImageView.setVisibility(View.INVISIBLE);
                Toast.makeText(EditFriendsActivity.this, "Contact has been removed", Toast.LENGTH_SHORT).show();

                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }

        }

    };

    private void checkEmail(final int position) throws NullPointerException {
    /**
     * Retrieve the email from the parse user object at the current position.
     * The current position will be the contact icon touched by the user
     * Prompt the user to enter an email address.  If the email matches
     * the current mUser object email, then a check mark will be added,
     * a toast message will appear, and the contact will be added to the friends list.
     * If the there is not a match then the user will receive a toast message.
     * @param int position
     * @return Nothing.
     */
        final String email = mUsers.get(position).getString("email");
        showEmailValidationTextField(Boolean.TRUE);

        mValidateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            //Preconditions.checkArgument(mContactEmail.getText().toString() != null);
            mEmail =  mContactEmail.getText().toString();

            if (mEmail.equals(email)) {
                Toast.makeText(EditFriendsActivity.this, "Success! " + email + " has been added to your friends.", Toast.LENGTH_SHORT).show();
                mFriendsRelation.add(mUsers.get(position));
                mCheckImageView.setVisibility(View.VISIBLE);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });


            } else {
                Toast.makeText(EditFriendsActivity.this, "Sorry, invalid email", Toast.LENGTH_SHORT).show();
                mGridView.setItemChecked(position,false);
            }
            showEmailValidationTextField(Boolean.FALSE);
        }
    });


    }

    private void showEmailValidationTextField(Boolean show) {
    /**
     * Display a text field for user input or remove the text field for user input
     * depending on the parameter, show.
     * @param boolean show
     * @return Nothing.
     */
        if (show){
            mGridView.setVisibility(View.INVISIBLE);
            mLayout.setVisibility(View.VISIBLE);
            mContactEmail.setVisibility(View.VISIBLE);
            mValidateEmail.setVisibility(View.VISIBLE);
        }
        else{
            mContactEmail.setText("");
            mContactEmail.setVisibility(View.INVISIBLE);
            mValidateEmail.setVisibility(View.INVISIBLE);
            mLayout.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }

    }

}

