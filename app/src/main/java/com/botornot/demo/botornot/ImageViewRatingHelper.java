package com.botornot.demo.botornot;

import android.widget.ImageView;

public class ImageViewRatingHelper {

    private ImageView mImageView;
    private Rating mRating;
    private String name = "Bottie";

    public ImageViewRatingHelper(ImageView imageView) {
        this(imageView, 0L, 0L, "");
    }

    public ImageViewRatingHelper(ImageView imageView, long imageId, long userId, String name) {
        this(imageView, new Rating(imageId, userId, 0.0));
        if (name != null) this.name = name;
    }

    public ImageViewRatingHelper(ImageView imageView, Rating rating) {
        mImageView = imageView;
        mRating = rating;
    }

    public long getImageId() {
        return mRating.getImageId();
    }

    public void setImageId(long imageId) {
        getRating().setImageId(imageId);
    }

    public long getUserId() {
        return mRating.getUserId();
    }

    public void setUserId(long userId) {
        getRating().setUserId(userId);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public Rating getRating() {
        return mRating;
    }

    public String getUserName() {
        return name;
    }
}










