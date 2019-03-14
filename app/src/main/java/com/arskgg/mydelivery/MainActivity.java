package com.arskgg.mydelivery;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button signUpBtn,signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpBtn = findViewById(R.id.signUpBtn);
        signInBtn = findViewById(R.id.signInBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignIn.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
            }
        });


        //Check If user checked Remember me and Log In
        //Init Paper for check if saved SignIn
        Paper.init(this);

        String User = Paper.book().read(Common.USER_KEY);
        String Password = Paper.book().read(Common.PWD_KEY);

        if (User != null && Password != null)
            logIn(User, Password);


        //SHare facebook
        printKeyHash();
    }



    private void logIn(final String phone, final String pwd) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference table_user = database.getReference("User");

        //Check if user connected to the Internet
        if (Common.isNetworkAvailable(getBaseContext()))
        {

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Check if user not exist in database
                    if (dataSnapshot.child(phone).exists())
                    {

                        //Get user information
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);

                        if(user.getPassword().equals(pwd))
                        {

                            Common.currentUser = user;
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            startActivity(intent);
                            finish();

                        }
                        else
                            Toast.makeText(MainActivity.this,"Wrong Password !", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {

                        Toast.makeText(MainActivity.this,"User not exist !", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
            Toast.makeText(MainActivity.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private void printKeyHash() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.arskgg.mydelivery",
                    PackageManager.GET_SIGNATURES);

            for(Signature signature : info.signatures){

                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
