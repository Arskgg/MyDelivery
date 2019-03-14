package com.arskgg.mydelivery.Model;

import java.util.List;

public class Request {

    private String phone;
    private String name;
    private String address;
    private String totalPrice;
    private String status;
    private String comment;
    private List<Order> orderList;

    public Request() {
    }

    public Request(String phone, String name, String address, String totalPrice, String comment, List<Order> orderList) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.totalPrice = totalPrice;
        this.status = "0"; // 0 is Default, 0: Placed, 1:Shipping, 2:Shipped
        this.comment = comment;
        this.orderList = orderList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}