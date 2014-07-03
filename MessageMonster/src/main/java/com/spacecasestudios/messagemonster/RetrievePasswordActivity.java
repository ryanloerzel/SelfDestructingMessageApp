package com.spacecasestudios.messagemonster;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class RetrievePasswordActivity extends Activity {

    //Declare member variables
    protected EditText mEmail;
    protected Button mRetrievePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_retrieve_password);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        mEmail = (EditText) findViewById(R.id.emailField);

        //When the button is clicked, invoke the Parse library password retrieval functionality
        mRetrievePasswordButton = (Button) findViewById(R.id.retrievePasswordButton);
        mRetrievePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                email = email.trim();

                if (email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RetrievePasswordActivity.this);
                    builder.setMessage(R.string.retrieve_password_error_message)
                            .setTitle(R.string.sign_up_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    setProgressBarIndeterminateVisibility(true);
                    //Retrieve the password by contacting the user through email
                    ParseUser.requestPasswordResetInBackground(email,
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                setProgressBarIndeterminateVisibility(false);
                                if (e == null) {
                                    // An email was successfully sent with reset instructions.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RetrievePasswordActivity.this);
                                    builder.setMessage(R.string.email_sent)
                                            .setTitle(R.string.success_title)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(RetrievePasswordActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                    AlertDialog dialog= builder.create();
                                    dialog.show();


                                } else {
                                    // Something went wrong. Look at the ParseException to see what's up.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RetrievePasswordActivity.this);
                                    builder.setMessage(e.getMessage())
                                            .setTitle(R.string.sign_up_error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog= builder.create();
                                    dialog.show();
                                }
                            }
                        }
                    );
                }
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.retrieve_password, menu);
        return true;
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
}
