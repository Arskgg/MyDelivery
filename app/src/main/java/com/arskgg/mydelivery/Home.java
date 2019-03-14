package com.arskgg.mydelivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Database.Database;
import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.Model.Category;
import com.arskgg.mydelivery.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView userName;

    RecyclerView recyclerMenu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference categoryTable;

    FirebaseRecyclerAdapter adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    CounterFab fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //SwipeLayout for refresh page
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
                    loadMenu();
                    swipeRefreshLayout.setRefreshing(false);
                }
                else{
                    Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
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
                    loadMenu();
                else{
                    Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        categoryTable = database.getReference("Category");


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Cart.class));
            }
        });

        //Set Fab count
        fab.setCount(new Database(this).getQuantityCart());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        //Communicate to the white color
        Menu menu = navigationView.getMenu();
        MenuItem tools= menu.findItem(R.id.communicateToolsTxt);
        SpannableString s = new SpannableString(tools.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TxtNavDrawer), 0, s.length(), 0);
        tools.setTitle(s);

        navigationView.setNavigationItemSelectedListener(this);

        //Set name of user in NavigationDrawer
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.userName);
        userName.setText(Common.currentUser.getName());

        //Load menu
        recyclerMenu = findViewById(R.id.recyclerMenu);
        recyclerMenu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(layoutManager);

        //Check if user connected to the Internet
        if (Common.isNetworkAvailable(getBaseContext()))
            loadMenu();
        else{
            Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
            return;
        }


        Paper.init(this);

    }


    private void loadMenu(){

        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(categoryTable, Category.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);

                return new MenuViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MenuViewHolder holder, int position, Category model) {

                //do binding stuff
                holder.menuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.menuImage);
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Check on internet connection
                        if (Common.isNetworkAvailable(getBaseContext()))
                        {
                            //Get Category Id and send to Food Activity
                            Intent foodList = new Intent(getApplicationContext(), FoodList.class);
                            //Because CategoryId is key, so we just get key of this item
                            foodList.putExtra("categoryId", adapter.getRef(position).getKey());
                            startActivity(foodList);
                        }
                        else{
                            Toast.makeText(Home.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                });
            }

        };

        adapter.notifyDataSetChanged();
        recyclerMenu.setAdapter(adapter);
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

    @Override
    protected void onResume() {
        super.onResume();
        //Set Fab count
        fab.setCount(new Database(this).getQuantityCart());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search)
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        }else if (id == R.id.nav_favorite) {

            startActivity(new Intent(getApplicationContext(), FavoritesActivity.class));

        } else if (id == R.id.nav_cart) {

            startActivity(new Intent(getApplicationContext(), Cart.class));

        } else if (id == R.id.nav_orders) {

            startActivity(new Intent(getApplicationContext(), OrderStatus.class));

        } else if (id == R.id.nav_log_out) {

            //Delete Remembered user
            Paper.book().destroy();

            //Log out
            Intent signOut = new Intent(getApplicationContext(), MainActivity.class);
            signOut.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signOut);

        } else if (id == R.id.nav_account) {

            startActivity(new Intent(getApplicationContext(), Account.class));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
