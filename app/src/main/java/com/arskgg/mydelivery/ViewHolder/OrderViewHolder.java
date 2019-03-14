package com.arskgg.mydelivery.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView orderId, orderStatus, orderPhone, orderAddress, orderDate;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        orderId = itemView.findViewById(R.id.orderId);
        orderStatus = itemView.findViewById(R.id.orderStatus);
        orderPhone = itemView.findViewById(R.id.orderPhone);
        orderAddress = itemView.findViewById(R.id.orderAddress);
        orderDate = itemView.findViewById(R.id.orderDate);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(),false);
    }


}
