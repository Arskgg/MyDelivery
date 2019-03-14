package com.arskgg.mydelivery;

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
import com.arskgg.mydelivery.Model.Request;
import com.arskgg.mydelivery.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerOrder;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Request");

        recyclerOrder = findViewById(R.id.recyclerOrder);
        recyclerOrder.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerOrder.setLayoutManager(layoutManager);

        //If we start OrderStatus Activity from Home Activity
        //We will not put any Extra, so we just loadOrder by phone  from Common
        if(getIntent() != null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone")); //this is in Notification
    }

    private void loadOrders(String phone) {

        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(requests.orderByChild("phone").equalTo(phone), Request.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_item, parent, false);

                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(OrderViewHolder holder, int position, Request model) {

                //do binding stuff
                holder.orderId.setText(adapter.getRef(position).getKey());
                holder.orderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.orderPhone.setText(model.getPhone());
                holder.orderAddress.setText(model.getAddress());
                holder.orderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });
            }

        };

        recyclerOrder.setAdapter(adapter);
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
