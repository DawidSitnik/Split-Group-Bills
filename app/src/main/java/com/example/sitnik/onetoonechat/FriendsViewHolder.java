package com.example.sitnik.onetoonechat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/***friends view holder*/
public class FriendsViewHolder extends RecyclerView.ViewHolder{

    View mView;

    public FriendsViewHolder(View itemView){
        super(itemView);
        mView = itemView;
    }

    /***sets name in layout with value from argument*/
    public void setName(String name){
        TextView userNameView = mView.findViewById(R.id.single_user_name);
        userNameView.setText(name);

    }
    /***sets status in layout with value from argument*/
    public void setStatus(String status){
        TextView userStatusView = mView.findViewById(R.id.single_user_status);
        userStatusView.setText(status);
    }

    /***sets thumb_image in layout with value from argument*/
    public void setThumbImage(String thumb_image){
        CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
    }



}
