package com.arskgg.mydelivery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Database.Database;
import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.Model.Favorite;
import com.arskgg.mydelivery.Model.Food;
import com.arskgg.mydelivery.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerFood;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodTable;
    DatabaseReference favoriteTable;
    FirebaseRecyclerAdapter adapter;

    String categoryId = "";

    //Search Functionality
    FirebaseRecyclerAdapter searchAdapter;
    List<String> suggestList = new ArrayList<>();

    MaterialSearchBar searchBar;

    //Favorites
    Database localDB;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodTable = database.getReference("Food");
        favoriteTable = database.getReference("Favorite");

        //LocalDb
        localDB = new Database(this);

        recyclerFood = findViewById(R.id.recyclerFood);
        recyclerFood.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerFood.setLayoutManager(layoutManager);

        //get intent
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("categoryId");
        if (!categoryId.isEmpty())
        {
            //Check if user connected to the Internet
            if (Common.isNetworkAvailable(getBaseContext()))
            loadListFood();
            else {
                Toast.makeText(FoodList.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //Search
        searchBar = findViewById(R.id.foodSearchBar);
        searchBar.setHint("Enter your food");

        loadSearchSuggest();
        searchBar.setLastSuggestions(suggestList);
        searchBar.setCardViewElevation(10);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //When users type their text, we will change suggest list
                List<String> suggest = new ArrayList<>();
                for (String search : suggestList) {
                    if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty())
                {
                    recyclerFood.setAdapter(adapter);
                    adapter.startListening();
                }
            }
        });

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When search bar is close
                //Restore original suggest adapter
                if (!enabled)
                {
                    recyclerFood.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result of search adapter
                if (!searchBar.getText().isEmpty())
                    startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


        //Swipe refresh layout to refresh information
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isNetworkAvailable(getBaseContext()))
                {
                    loadListFood();
                    swipeRefreshLayout.setRefreshing(false);
                }
                else{
                    Toast.makeText(FoodList.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
            }
        });

        //Default load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isNetworkAvailable(getBaseContext()))
                    loadListFood();
                else{
                    Toast.makeText(FoodList.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }


    private void loadListFood() {

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                        // Select from table where menuId == categoryId
                        .setQuery(foodTable.orderByChild("menuId").equalTo(categoryId), Food.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.food_item, viewGroup, false);

                return new FoodViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, final Food model) {

                holder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.foodImg);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(getApplicationContext(), FoodDetail.class);
                        //Put element ID which was clicked
                        foodDetail.putExtra("foodId",adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });

                //Add to the favorites
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

                                       if (!dataSnapshot.exists())
                                       {
                                           Favorite favorite = new Favorite(
                                                   model.getName(),
                                                   model.getImage(),
                                                   model.getDescription(),
                                                   model.getPrice(),
                                                   model.getDiscount(),
                                                   model.getMenuId(),
                                                   adapter.getRef(position).getKey());
                                           favoriteTable.child(Common.currentUser.getPhone()).push().setValue(favorite);
                                           holder.favoriteImg.setImageResource(R.drawable.ic_favorite_checked);
                                       }
                                       else {

                                           for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                               postSnapshot.getRef().removeValue();
                                               holder.favoriteImg.setImageResource(R.drawable.ic_favorite_unchecked);

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
        recyclerFood.setAdapter(adapter);
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

    private void loadSearchSuggest() {

        foodTable.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName()); // Add name of food to collection
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startSearch(CharSequence text) {

        Query query = foodTable.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                // Select from table where menuId == categoryId
                .setQuery(foodTable.orderByChild("name").equalTo(text.toString()), Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.food_item, viewGroup, false);

                return new FoodViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, Food model) {

                holder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.foodImg);
                final Food clickItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(getApplicationContext(), FoodDetail.class);
                        //Put element ID which was clicked
                        foodDetail.putExtra("foodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });
            }
        };
        recyclerFood.setAdapter(searchAdapter);
        searchAdapter.startListening();
    }

}
