package com.arskgg.mydelivery.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView foodName;
    public ImageView foodImg, favoriteImg;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        foodName = itemView.findViewById(R.id.foodItemTxt);
        foodImg = itemView.findViewById(R.id.foodItemImg);
        favoriteImg = itemView.findViewById(R.id.favoriteBtn);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(), false );
    }
}
