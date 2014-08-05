package com.spacecasestudios.messagemonster.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class ViewImageActivity extends Activity {

    ProgressBar mProgressBar;
    TextView mTextMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextMessageView = (TextView)findViewById(R.id.textMessageView);

        String messageType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        if (messageType.equals(ParseConstants.TYPE_TEXT)){
            mTextMessageView.setText(getIntent().getExtras().get(ParseConstants.KEY_TEXT_MESSAGE).toString());
            mProgressBar.setVisibility(View.INVISIBLE);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                }
            }, 10 * 1000);
        }
        else {

            Uri imageUri = getIntent().getData();

            Picasso.with(this).load(imageUri.toString()).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 10 * 1000);
                }

                @Override
                public void onError() {

                }
            });
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 20*1000);
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
