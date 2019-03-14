package com.arskgg.mydelivery.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.arskgg.mydelivery.Cart;
import com.arskgg.mydelivery.Common.Common;
import com.arskgg.mydelivery.Database.Database;
import com.arskgg.mydelivery.Interface.ItemClickListener;
import com.arskgg.mydelivery.Model.Order;
import com.arskgg.mydelivery.R;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData;
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(cart).inflate(R.layout.cart_item, viewGroup, false);

        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, final int position) {

        cartViewHolder.cartName.setText(listData.get(position).getProductName());

        //Make Id to Image
        //TextDrawable drawable = TextDrawable.builder().buildRound("" + listData.get(position).getQuantity(), Color.RED);
        //cartViewHolder.cartCountImg.setImageDrawable(drawable);

        cartViewHolder.quantityBtn.setNumber(listData.get(position).getQuantity());

        cartViewHolder.quantityBtn.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //Update total Price
                int total = 0;
                List<Order> orders = new Database(cart).getCart();
                for (Order item : orders)
                    total +=(Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));

                Locale locale = new Locale("en","US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                cart.totalPrice.setText(fmt.format(total));
            }
        });


        //Make Price to currency format
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuantity()));
        cartViewHolder.cartPrice.setText(fmt.format(price));

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    public TextView cartName, cartPrice;
    public ElegantNumberButton quantityBtn;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        cartName = itemView.findViewById(R.id.cartItemName);
        cartPrice = itemView.findViewById(R.id.cartItemPrice);
        quantityBtn = itemView.findViewById(R.id.quantityBtn);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select your action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}