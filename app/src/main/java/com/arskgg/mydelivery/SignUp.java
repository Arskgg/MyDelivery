package com.arskgg.mydelivery;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    EditText mPhone, mName, mPassword;
    Button mSignUpBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mPhone = findViewById(R.id.edtPhone);
        mName = findViewById(R.id.edtName);
        mPassword = findViewById(R.id.edtPassword);

        mSignUpBtn = findViewById(R.id.signUp);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if user connected to the Internet
                if (Common.isNetworkAvailable(getBaseContext()))
                {

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                            //Check if user exist
                            if (dataSnapshot.child(mPhone.getText().toString()).exists())
                            {
                                Toast.makeText(SignUp.this, "Phone Number already exist", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                //Add user to the database
                                User user = new User(mName.getText().toString(), mPassword.getText().toString());
                                table_user.child(mPhone.getText().toString()).setValue(user);
                                Toast.makeText(SignUp.this, "Sign up successful !", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignUp.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
