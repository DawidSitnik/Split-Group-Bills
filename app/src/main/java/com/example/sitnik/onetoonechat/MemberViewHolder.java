package com.example.sitnik.onetoonechat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberViewHolder extends RecyclerView.ViewHolder{

    View mView;


    public View getmView() {
        return mView;
    }

    public MemberViewHolder(View itemView){
        super(itemView);
        mView = itemView;
    }

    public void setName(String name){
        TextView singleName = mView.findViewById(R.id.single_member_name);
        singleName.setText(name);
    }

    public void setThumbImage(String thumb_image) {
        CircleImageView userImageView = mView.findViewById(R.id.single_member_image);
        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
    }

    public String getName(){
        TextView singleName = mView.findViewById(R.id.single_member_name);
        return singleName.getText().toString();
    }

}
