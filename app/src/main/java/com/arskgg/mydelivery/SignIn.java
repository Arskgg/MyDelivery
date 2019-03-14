package com.arskgg.mydelivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;


public class SignIn extends AppCompatActivity {

    EditText mPhone, mPassword;
    Button mSignInBtn;
    CheckBox rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mPhone = findViewById(R.id.edtPhone);
        mPassword = findViewById(R.id.edtPassword);
        mSignInBtn = findViewById(R.id.signIn);

        rememberMe = findViewById(R.id.rememberMe);

        //Init Paper for save SignIn
        Paper.init(this);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if user connected to the Internet
                if (Common.isNetworkAvailable(getBaseContext()))
                {
                    //Save SignIn user if he checked RememberMe
                    if (rememberMe.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY, mPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, mPassword.getText().toString());
                    }

                    //SignIn process
                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //Check if user not exist in database
                            if (dataSnapshot.child(mPhone.getText().toString()).exists())
                            {

                                //Get user information
                                User user = dataSnapshot.child(mPhone.getText().toString()).getValue(User.class);
                                user.setPhone(mPhone.getText().toString());

                                if(user.getPassword().equals(mPassword.getText().toString()))
                                {

                                    Common.currentUser = user;
                                    Intent intent = new Intent(getApplicationContext(), Home.class);
                                    startActivity(intent);
                                    finish();

                                }
                                else
                                    Toast.makeText(SignIn.this,"Wrong Password !", Toast.LENGTH_SHORT).show();

                            }
                            else
                            {

                                Toast.makeText(SignIn.this,"User not exist !", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else {
                    Toast.makeText(SignIn.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
