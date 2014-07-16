package com.spacecasestudios.messagemonster.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spacecasestudios.messagemonster.MessageMonsterApplication;
import com.spacecasestudios.messagemonster.R;

import static android.view.Window.FEATURE_INDETERMINATE_PROGRESS;


public class LoginActivity extends Activity {
    protected EditText mUserName;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected Button mSignUpButton;
    protected ProgressBar mProgressBar;
    protected TextView mRetrievePasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.hide();

        mSignUpButton = (Button)findViewById(R.id.registerButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });

        mRetrievePasswordTextView = (TextView) findViewById(R.id.forgotPasswordText);
        mRetrievePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RetrievePasswordActivity.class);
                startActivity(intent);

            }
        });


        mUserName = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText)findViewById(R.id.passwordField);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUserName.getText().toString();
                String password = mPassword.getText().toString();

                username = username.trim();
                password = password.trim();

                if(username.isEmpty() || password.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.sign_up_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog= builder.create();
                    dialog.show();
                }
                else{
                    //Attempt to Login the user
                    mProgressBar.setVisibility(View.VISIBLE);
                    ParseUser.logInInBackground(username,password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            if(e == null){

                                //Success!
                                MessageMonsterApplication.updateParseInstallation(user);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.sign_up_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog= builder.create();
                                dialog.show();
                            }
                        }
                    });

                }
            }
        });
    }
}
