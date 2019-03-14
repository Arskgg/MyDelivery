package com.arskgg.mydelivery;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class Account extends AppCompatActivity {

    TextView name, phone , changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        changePassword = findViewById(R.id.changePassword);

        name.setText(Common.currentUser.getName());
        phone.setText(Common.currentUser.getPhone());

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               changeUserPassword();
            }
        });

        Paper.init(this);

    }

    private void changeUserPassword() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(Account.this);
        dialog.setTitle("Change Password");

        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.password_changing, null);

        final EditText oldPassword = v.findViewById(R.id.edtOldPassword);
        final EditText newPassword = v.findViewById(R.id.edtNewPassword);
        final EditText newPasswordRepeat = v.findViewById(R.id.edtRepeatNewPassword);

        dialog.setView(v);
        dialog.setIcon(R.drawable.ic_key);


        dialog.setPositiveButton("APPLY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!Common.currentUser.getPassword().equals(oldPassword.getText().toString()))
                    Toast.makeText(Account.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                else
                {
                    if (newPassword.getText().toString().equals(newPasswordRepeat.getText().toString())
                            && newPassword.getText().toString().length() > 0)
                    {
                        Common.currentUser.setPassword(newPassword.getText().toString());

                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", newPassword.getText().toString());

                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone()).updateChildren(passwordUpdate);

                        //rewrite remember me
                        String Password = Paper.book().read(Common.PWD_KEY);
                        if(Password != null)
                            Paper.book().write(Common.PWD_KEY, newPassword.getText().toString());

                        Toast.makeText(Account.this, "Password successfully changed ", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(Account.this, "New passwords are not match or have small length", Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

}
