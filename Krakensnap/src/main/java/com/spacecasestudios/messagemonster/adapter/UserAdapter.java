package com.spacecasestudios.messagemonster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.spacecasestudios.messagemonster.R;
import com.spacecasestudios.messagemonster.utilities.MD5Util;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Ryan on 6/21/2014.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mUsers;
    protected TextView mView;

    public UserAdapter(Context context, List<ParseUser> users){
        super(context, R.layout.message_item, users);
        mContext = context;
        mUsers = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            holder.userImageView = (ImageView)convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            holder.checkedImageView = (ImageView)convertView.findViewById(R.id.checkImageView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        ParseUser user = mUsers.get(position);
        String email = user.getEmail().toLowerCase();

        if (email.equals("")) {
            holder.userImageView.setImageResource(R.drawable.avatar_empty);
        }
        else {
            String hash = MD5Util.md5Hex(email);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash +
                    "?s=204&d=404";

            Picasso.with(mContext).load(gravatarUrl).placeholder(R.drawable.avatar_empty).into(holder.userImageView);
        }

        holder.nameLabel.setText(user.getUsername());

        GridView gridView = (GridView)parent;
        if(gridView.isItemChecked(position)){
            holder.checkedImageView.setVisibility(View.VISIBLE);
        }
        else{
            holder.checkedImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private static class ViewHolder{
        ImageView userImageView;
        ImageView checkedImageView;
        TextView nameLabel;
    }

    public void refill(List<ParseUser> users){
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
