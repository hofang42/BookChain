package com.hofang.bookchainfe.model;

import java.io.Serializable;

public class Address implements Serializable {
    private String id;
    private String userId;
    private String recipientName;
    private String phoneNumber;
    private String street;
    private String city;
    private String district;
    private String ward;
    private String postalCode;
    private boolean isDefault;

    public Address() {
    }

    public Address(String id, String userId, String recipientName, String phoneNumber,
                   String street, String city, String district, String ward,
                   String postalCode, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0) sb.append(",\n");
            sb.append(district);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(",\n");
            sb.append(postalCode);
        }
        return sb.toString();
    }
}
