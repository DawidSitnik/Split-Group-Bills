package com.example.sitnik.onetoonechat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupBillViewHolder extends RecyclerView.ViewHolder{

    View mView;

    public GroupBillViewHolder(View itemView){
        super(itemView);
        mView = itemView;
    }

    public void setDescription(String description){
        TextView singleDescription = mView.findViewById(R.id.bill_single_description);
        singleDescription.setText(description);
    }

    public void setWhoPaid(String paidByName){

        TextView singleWhoLend = mView.findViewById(R.id.bill_single_ows);
        singleWhoLend.setText(paidByName+ " paid " + ShareWithGroup.billAmount + "zl");
    }

    public void setYourPayoff(String billUserAmount) {

        TextView payoffField = mView.findViewById(R.id.single_bill_whoLend);
        if(billUserAmount.equals(""))billUserAmount="0";
        double billUserAmountDouble = Double.parseDouble(billUserAmount);
        String payoffText;

        if(billUserAmountDouble < 0){
            billUserAmountDouble = -billUserAmountDouble;
            payoffText = "You borrowed " + billUserAmountDouble + "zl";
        } else{
            payoffText = "You lend " + billUserAmountDouble + "zl";
        }

        payoffField.setText(payoffText);
    }

    public void setSettleUpImage(){
        CircleImageView mImage = mView.findViewById(R.id.bill_single_image);
        mImage.setImageResource(R.drawable.settle_up);
    }

    public void setDefaultImage() {
        CircleImageView mImage = mView.findViewById(R.id.bill_single_image);
        mImage.setImageResource(R.drawable.bill);
    }

    public void setWhoLendEmpty(){
        TextView singleWhoLend = mView.findViewById(R.id.single_bill_whoLend);
        singleWhoLend.setText("");
    }
}
