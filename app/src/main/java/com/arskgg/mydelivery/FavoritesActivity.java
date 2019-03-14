package com.arskgg.mydelivery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.Model.Favorite;
import com.arskgg.mydelivery.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class FavoritesActivity extends AppCompatActivity {

    RecyclerView recyclerFavorites;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter adapter;

    FirebaseDatabase database;
    DatabaseReference foodTable, favoriteTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //Firebase
        database = FirebaseDatabase.getInstance();
        favoriteTable = database.getReference("Favorite");

        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        recyclerFavorites.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerFavorites.setLayoutManager(layoutManager);


        loadFavoriteList();

    }



    private void loadFavoriteList() {

        FirebaseRecyclerOptions<Favorite> options = new FirebaseRecyclerOptions.Builder<Favorite>()
                .setQuery(favoriteTable.child(Common.currentUser.getPhone()), Favorite.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Favorite, FoodViewHolder>(options) {

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.food_item, viewGroup, false);

                return new FoodViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Favorite model) {
                holder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.foodImg);



                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(getApplicationContext(), FoodDetail.class);
                        //Put element ID which was clicked
                        foodDetail.putExtra("foodId",model.getFoodId());
                        startActivity(foodDetail);

                    }
                });

                //Add to the favorite
                favoriteTable.child(Common.currentUser.getPhone())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                                {
                                    Favorite item = postSnapshot.getValue(Favorite.class);
                                    if (item.getName().equals(model.getName()))
                                        holder.favoriteImg.setImageResource(R.drawable.ic_favorite_checked);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                //Click to change state of favorite
                holder.favoriteImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        favoriteTable.child(Common.currentUser.getPhone()).orderByChild("name").equalTo(model.getName())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists())
                                        {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                postSnapshot.getRef().removeValue();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        recyclerFavorites.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
