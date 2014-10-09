/**
* File CreateMessageActivity.java
* Author: Ryan Loerzel
* Created: July 16, 2014
* Description: Allows users to input text, which will later be sent as message
*/

package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

public class CreateMessageActivity extends Activity {

    // Declare ui variables
    protected EditText mMessageText;
    protected Button mSendTextMessage;
    protected String mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        //initiate ui variables
        mMessageText = (EditText)findViewById(R.id.editText);
        mSendTextMessage = (Button) findViewById(R.id.sendTextMessage);
        mSendTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextMessage();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_message, menu);
        //mSendMenuItem = menu.getItem(0);
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
                sendTextMessage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendTextMessage() {
        /**
         * Prompts user to enter text and checks that the
         * message length is greater than or equal to one character.
         * User will be taken to the Recipients Activity to choose a message recipient
         * @param Nothing.
         * @return Nothing.
         */

        //noinspection ConstantConditions
        mMessage =  mMessageText.getText().toString();
        int length = mMessageText.getText().length();
        if(length < 1){
            Toast.makeText(CreateMessageActivity.this, "Please enter a message.", Toast.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(CreateMessageActivity.this, RecipientsActivity.class);
            intent.putExtra("Text Message", mMessage);
            intent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_TEXT);
            startActivity(intent);
            finish();
        }
    }

}
