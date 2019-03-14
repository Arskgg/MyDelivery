package com.arskgg.mydelivery.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arskgg.mydelivery.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public TextView userName, userComment;
    public RatingBar userRating;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

       userName = itemView.findViewById(R.id.userName);
       userComment = itemView.findViewById(R.id.comment);
       userRating = itemView.findViewById(R.id.userRating);

    }
}
