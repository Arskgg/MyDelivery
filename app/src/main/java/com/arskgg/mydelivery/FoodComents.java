package com.arskgg.mydelivery;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Model.Rating;
import com.arskgg.mydelivery.ViewHolder.CommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FoodComents extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ratingTable;

    RecyclerView recyclerFoodComments;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter adapter;

    SwipeRefreshLayout refreshLayout;

    String foodId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_coments);

        //Firebase
        database = FirebaseDatabase.getInstance();
        ratingTable = database.getReference("Rating");

        recyclerFoodComments = findViewById(R.id.recyclerFoodComments);
        recyclerFoodComments.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerFoodComments.setLayoutManager(layoutManager);

        //Load comments
        if (getIntent() != null)
        {
            foodId = getIntent().getStringExtra("foodId");
            loadComments();
        }

        //Refresh layout
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isNetworkAvailable(getBaseContext()))
                {
                    loadComments();
                    refreshLayout.setRefreshing(false);
                }
                else{
                    Toast.makeText(FoodComents.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                }


            }
        });





    }

    private void loadComments() {

        Query query = ratingTable.orderByChild("foodId").equalTo(foodId);

        FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                .setQuery(query, Rating.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.food_comment_item, viewGroup, false);

                return new CommentViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {

                holder.userName.setText(model.getUserName());
                holder.userComment.setText(model.getComment());
                holder.userRating.setRating(Float.parseFloat(model.getRatingValue()));
            }
        };

        adapter.startListening();

        recyclerFoodComments.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
