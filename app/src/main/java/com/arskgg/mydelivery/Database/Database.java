package com.arskgg.mydelivery.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.arskgg.mydelivery.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "myDelivery.db";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME,null, DB_VERSION);
    }

    public List<Order> getCart()
    {
        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"_id","productName", "productId", "quantity", "price", "discount"};
        String sqlTable = "orderDetail";

        qb.setTables(sqlTable);

        Cursor cursor = qb.query(database, sqlSelect,null,null,null, null, null);

        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {

                result.add(new Order(cursor.getInt(cursor.getColumnIndex("_id")),
                                    cursor.getString(cursor.getColumnIndex("productId")),
                                    cursor.getString(cursor.getColumnIndex("productName")),
                                    cursor.getString(cursor.getColumnIndex("quantity")),
                                    cursor.getString(cursor.getColumnIndex("price")),
                                    cursor.getString(cursor.getColumnIndex("discount"))
                                    ));

            }while(cursor.moveToNext());

        }

        return result;
    }

    public void addToCart(Order order){

        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT INTO orderDetail(productName,productId,quantity,price,discount) VALUES('%s','%s','%s','%s','%s')",
                order.getProductName(),
                order.getProductId(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount());

        database.execSQL(query);
    }

    public void clearCart(){

        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM orderDetail");

        database.execSQL(query);
    }

    //Favorites
    public int getCountCart(){

        int counter = 0;
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT COUNT(*) FROM orderDetail";
        Cursor cursor = database.rawQuery(query,null);

        if (cursor.moveToFirst()){

            do {
                counter = cursor.getInt(0);

            }while (cursor.moveToNext());
        }
        cursor.close();

        return counter;
    }

    public int getQuantityCart(){

        int counter = 0;
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT * FROM orderDetail";
        Cursor cursor = database.rawQuery(query,null);

        if (cursor.moveToFirst()){

            do {
                counter += Integer.valueOf(cursor.getString(cursor.getColumnIndex("quantity")));

            }while (cursor.moveToNext());
        }
        cursor.close();

        return counter;
    }

    public void updateCart(Order order) {

        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("UPDATE orderDetail SET quantity= %s WHERE _id = %d", order.getQuantity(), order.getID());
        database.execSQL(query);
    }


}

