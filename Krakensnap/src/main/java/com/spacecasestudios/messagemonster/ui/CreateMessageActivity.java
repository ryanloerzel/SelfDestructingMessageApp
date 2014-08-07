package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

public class CreateMessageActivity extends Activity {
    protected EditText mMessageText;
    protected Button mSendTextMessage;
    protected String mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);

        mMessageText = (EditText)findViewById(R.id.editText);
        mSendTextMessage = (Button) findViewById(R.id.sendTextMessage);

        mSendTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessage = mMessageText.getText().toString();

                if(mMessage.equals("")){
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
        });


    }

}
