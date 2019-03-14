package com.arskgg.mydelivery;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Database.Database;
import com.arskgg.mydelivery.Model.Order;
import com.arskgg.mydelivery.Model.Request;
import com.arskgg.mydelivery.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {

    Button placeOrderBtn;
    public TextView totalPrice;

    RecyclerView recyclerCart;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requestToFirebase;

    List<Order> cart =  new ArrayList<>();
    CartAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requestToFirebase = database.getReference("Request");

        //Init

        totalPrice = findViewById(R.id.totalPrice);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        recyclerCart = findViewById(R.id.recyclerCart);
        recyclerCart.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerCart.setLayoutManager(layoutManager);

        loadCartList();

        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty !", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void showAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Cart.this);
        dialog.setTitle("One more step !");
        dialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.ordeer_address_comment, null);

        final EditText edtAddress = view.findViewById(R.id.edtAddress);
        final EditText edtComment = view.findViewById(R.id.edtComment);

        dialog.setView(view);
        dialog.setIcon(R.drawable.ic_shopping_cart);

        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        totalPrice.getText().toString(),
                        edtComment.getText().toString(),
                        cart
                );

                //Submit to Firebase
                //We will use System.currentTimeMillis() to key
                String orderNumber = String.valueOf(System.currentTimeMillis());
                requestToFirebase.child(orderNumber).setValue(request);

                //Clear Cart
                new Database(getApplicationContext()).clearCart();

                loadCartList();

                Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                dialog.dismiss();


            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void loadCartList() {

        cart = new Database(this).getCart();

        adapter = new CartAdapter(cart,this);
        recyclerCart.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        //Calculate total Price
        int total = 0;
        for (Order order : cart)
            total +=(Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));

        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        totalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());

        return true;
    }

    private void deleteCart(int position) {
        //We will remove item at List<Order>
        cart.remove(position);
        //Delete data from SQLite
        new Database(this).clearCart();
        //And final, update new data from List<Order> to SQLite
        for (Order item : cart) {
            new Database(this).addToCart(item);
        }

        //Refresh
        loadCartList();

    }


}
