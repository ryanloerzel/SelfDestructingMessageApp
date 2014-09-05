package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
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

        //***********************************************************************************
        //Retrieve a List of of ParseUser Objects from the parse.com, sorted by username
        //Set the progress bar to visible while loading and invisible when finished
        //Assign the ParseUser List to the mUsers variable and create an array of user names
        //Set the Grid view to hold the Parse user names
        //***********************************************************************************
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        setProgressBarIndeterminateVisibility(true);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    //success
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //***********************************************************************************
    //For each Parse user that is a friend of the current user, overlay a check mark
    //image on that friends photo
    //***********************************************************************************
    private void addFriendCheckmarks() {
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
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            mCheckImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {

               checkEmail(position);

            } else {
                //remove
                mFriendsRelation.remove(mUsers.get(position));
                mCheckImageView.setVisibility(View.INVISIBLE);
                Toast.makeText(EditFriendsActivity.this, "Not Checked!", Toast.LENGTH_SHORT).show();

                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
            Toast.makeText(EditFriendsActivity.this, "Outside", Toast.LENGTH_SHORT).show();
            //               This was originally here but I had to move it to two different spots to get it to save a friend if emails matched
            //                mCurrentUser.saveInBackground(new SaveCallback() {
            //                @Override
            //                public void done(ParseException e) {
            //                    if (e != null) {
            //                        Log.e(TAG, e.getMessage());
            //                    }
            //                }
            //            });


        }

    };

    private void checkEmail(final int position) {
        final String email = mUsers.get(position).getString("email");
        showEmailValidationTextField(Boolean.TRUE);

        mValidateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmail = mContactEmail.getText().toString();
                if (mEmail.equals(email)) {
                    Toast.makeText(EditFriendsActivity.this, "Match! " + email, Toast.LENGTH_SHORT).show();
                    mFriendsRelation.add(mUsers.get(position));
                    mCheckImageView.setVisibility(View.VISIBLE);
                    System.out.print("Inside");
                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });


                } else {
                    Toast.makeText(EditFriendsActivity.this, "Bummer! " + email, Toast.LENGTH_SHORT).show();
                    mGridView.setItemChecked(position,false);
                }
                showEmailValidationTextField(Boolean.FALSE);
            }
        });


    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
        }
    }

    private void showEmailValidationTextField(Boolean show) {
        if (show){
            mGridView.setVisibility(View.INVISIBLE);
            mLayout.setVisibility(View.VISIBLE);
            mContactEmail.setVisibility(View.VISIBLE);
            mValidateEmail.setVisibility(View.VISIBLE);
        }
        else{
            mContactEmail.setVisibility(View.INVISIBLE);
            mValidateEmail.setVisibility(View.INVISIBLE);
            mLayout.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }

    }

}







//        private String emailValidation() {
//            final String[] google = new String[1];
//            AlertDialog.Builder alert = new AlertDialog.Builder(EditFriendsActivity.this);
//
//
//            alert.setTitle("Add a contact");
//            alert.setMessage("Please enter this contact's email address to add");
//
//            // Set an EditText view to get user input
//            final EditText input = new EditText(EditFriendsActivity.this);
//            alert.setView(input);
//
//
//            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    String value = String.valueOf(input.getText());
//                    google[0] = value;
//                }
//            });
//
//            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    // Canceled.
//                }
//            });
//
//            alert.show();
//            return google[0];
//
//        }