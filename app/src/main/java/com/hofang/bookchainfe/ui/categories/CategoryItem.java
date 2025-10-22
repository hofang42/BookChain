package com.hofang.bookchainfe.ui.categories;

public class CategoryItem {
    private String name;
    private int imageResId;

    public CategoryItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}