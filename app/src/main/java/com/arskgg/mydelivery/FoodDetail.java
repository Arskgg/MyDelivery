package com.arskgg.mydelivery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Database.Database;
import com.arskgg.mydelivery.Model.Food;
import com.arskgg.mydelivery.Model.Order;
import com.arskgg.mydelivery.Model.Rating;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView foodName, foodPrice, foodDescription;
    ImageView foodImg;
    Button commentsBtn;

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton cartBtn, ratingBtn;
    ElegantNumberButton numberBtn;
    RatingBar ratingBar;

    FirebaseDatabase database;
    DatabaseReference foodTable;
    DatabaseReference ratingTable;

    String foodId = "";

    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodTable = database.getReference("Food");
        ratingTable = database.getReference("Rating");

        //Inuit view
        numberBtn = findViewById(R.id.numberCounter);
        cartBtn = findViewById(R.id.cartBtn);
        ratingBtn = findViewById(R.id.ratingBtn);
        ratingBar = findViewById(R.id.ratingBar);
        commentsBtn = findViewById(R.id.commentsBtn);


        foodName = findViewById(R.id.foodName);
        foodPrice = findViewById(R.id.foodPrice);
        foodDescription = findViewById(R.id.foodDescription);

        foodImg = findViewById(R.id.foodImg);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberBtn.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));

                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        ratingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent foodComments = new Intent(getApplicationContext(), FoodComents.class);
                foodComments.putExtra("foodId",foodId);
                startActivity(foodComments);
            }
        });

        //Get food ID from Intent
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("foodId");
        }
        if(!foodId.isEmpty())
        {
            //Check if user connected to the Internet
            if (Common.isNetworkAvailable(getBaseContext()))
            {
                loadFoodDetail(foodId);
                loadFoodRating(foodId);
            }
            else {
                Toast.makeText(FoodDetail.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }


    private void loadFoodDetail(String foodId) {

        foodTable.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(foodImg);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                foodPrice.setText(currentFood.getPrice());
                foodName.setText(currentFood.getName());
                foodDescription.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadFoodRating(String foodId) {

        Query foodRating = ratingTable.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {

            int counter = 0, sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRatingValue());
                    counter++;
                }

                if (counter != 0)
                {
                    float averageRating = sum/counter;
                    ratingBar.setRating(averageRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Bad","Quite OK","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }


    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comment) {
        //Get rating and upload to firebase
        final Rating rating = new Rating(
                Common.currentUser.getPhone(),
                Common.currentUser.getName(),
                foodId,
                String.valueOf(value),
                comment);

        ratingTable.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    }
                });



        //There is code when user can feedback one rating

        /*ratingTable.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove old value
                    ratingTable.child(Common.currentUser.getPhone()).removeValue();
                    //Update value
                    ratingTable.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else
                    ratingTable.child(Common.currentUser.getPhone()).setValue(rating);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

}
