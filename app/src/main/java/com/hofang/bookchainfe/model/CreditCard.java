package com.hofang.bookchainfe.model;

public class CreditCard {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardholderName;
    private String billingAddress;
    private String postalCode;

    public CreditCard() {
    }

    public CreditCard(String cardNumber, String expiryDate, String cvv,
                      String cardholderName, String billingAddress, String postalCode) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardholderName = cardholderName;
        this.billingAddress = billingAddress;
        this.postalCode = postalCode;
    }

    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    // Validation methods
    public boolean isValid() {
        return isCardNumberValid() && isExpiryDateValid() && 
               isCvvValid() && isCardholderNameValid() && 
               isBillingAddressValid();
    }

    public boolean isCardNumberValid() {
        return cardNumber != null && 
               cardNumber.replaceAll("\\s", "").length() >= 13 && 
               cardNumber.replaceAll("\\s", "").length() <= 19;
    }

    public boolean isExpiryDateValid() {
        if (expiryDate == null || expiryDate.length() != 5) {
            return false;
        }
        String[] parts = expiryDate.split("/");
        if (parts.length != 2) {
            return false;
        }
        try {
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            return month >= 1 && month <= 12 && year >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isCvvValid() {
        return cvv != null && (cvv.length() == 3 || cvv.length() == 4);
    }

    public boolean isCardholderNameValid() {
        return cardholderName != null && !cardholderName.trim().isEmpty();
    }

    public boolean isBillingAddressValid() {
        return billingAddress != null && !billingAddress.trim().isEmpty();
    }

    // Utility methods
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("\\s", "");
        return "**** **** **** " + cleaned.substring(cleaned.length() - 4);
    }
}
