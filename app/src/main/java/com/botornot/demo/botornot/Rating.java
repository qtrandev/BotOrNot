package com.botornot.demo.botornot;

public class Rating {

    private long mImageid;
    private long mUserId;
    private double mRating;

    public Rating() {
        this(0,0,0.0);
    }

    public Rating(long imageId, long userId, double rating) {
        mImageid = imageId;
        mUserId = userId;
        mRating = rating;
    }

    public long getImageId() {
        return mImageid;
    }

    public void setImageId(long imageId) {
        mImageid = imageId;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public double getRating() {
        return mRating;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    @Override
    public String toString() {
        return "Image: "+getImageId()+", User: "+getUserId()+", Rating: "+getRating();
    }
}








