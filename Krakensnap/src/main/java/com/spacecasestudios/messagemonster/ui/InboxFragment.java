/**************************************************************************************************
 * File InboxFragment.java
 * Author: Ryan Loerzel
 * Created: July 16, 2014
 * Description: Retrieve Messages
 **************************************************************************************************/
package com.spacecasestudios.messagemonster.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.adapter.MessageAdapter;
import com.spacecasestudios.messagemonster.utilities.ParseConstants;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends ListFragment {
    protected List<ParseObject> mMessages;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ImageView mEmptyImage;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mEmptyImage = (ImageView)rootView.findViewById(R.id.emptyImage);

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.sea_green, R.color.lime_green, R.color.sea_green, R.color.lime_green);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);
        retrieveMessages();

    }

    private void retrieveMessages() {
        //Query the Parse database for messages that match the current user id
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                //getActivity().setProgressBarIndeterminateVisibility(false); //THROWING AN EXCEPTION
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                if(e==null){
                    //We found messages!
                    mMessages = messages;

                    if(mMessages.size() == 0){
                        mEmptyImage.setVisibility(View.VISIBLE);
                        
                    }
                    else {
                        mEmptyImage.setVisibility(View.INVISIBLE);
                    }

                    String[] usernames = new String[mMessages.size()];
                    int i = 0;
                    for (ParseObject message : mMessages) {
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }
                    if(getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(
                                getListView().getContext(),
                                mMessages);
                        setListAdapter(adapter);
                    }
                    else{
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);
                    }
                }
            }
        });
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        Uri fileUri = null;
        String textMessage = null;

        if(!messageType.equals(ParseConstants.TYPE_TEXT)){
            fileUri = Uri.parse(file.getUrl());
        }
        else {
            textMessage =  message.getString(ParseConstants.KEY_TEXT_MESSAGE);
        }

        if (messageType.equals(ParseConstants.TYPE_IMAGE)){
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.putExtra(ParseConstants.KEY_FILE_TYPE, messageType);
            intent.setData(fileUri);
            startActivity(intent);
        }
        else if(messageType.equals(ParseConstants.TYPE_TEXT)){
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.putExtra(ParseConstants.KEY_FILE_TYPE, messageType);
            intent.putExtra(ParseConstants.KEY_TEXT_MESSAGE, textMessage);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.putExtra(ParseConstants.KEY_FILE_TYPE, messageType);
            intent.setDataAndType(fileUri, "video/*");
            startActivity(intent);
        }

        //Delete Message
        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if(ids.size() == 1){
            //Last recipient: delete whole thing
            message.deleteInBackground();
        }
        else{
            // remove recipient and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());
            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();
        }
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
            Toast.makeText(getActivity(), "We're refreshing!", Toast.LENGTH_LONG).show();
        }
    };
}
