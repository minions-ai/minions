package com.minionsai.claim.dto;

public class ClaimIntakeRequest {

  String claimNumber;
  String claimant_first_name;
  String claimant_last_name;
  String claimant_email;
  String claimant_phone;
  String accident_location;
  String accident_latitude;
  String accident_longitude;
  String policy_number;
  String fraud_score;

  public String validate() {
    StringBuilder missingFields = new StringBuilder();

    if (isNullOrEmpty(claimNumber)) {
      missingFields.append("claimNumber, ");
    }
    if (isNullOrEmpty(claimant_first_name)) {
      missingFields.append("claimant_first_name, ");
    }
    if (isNullOrEmpty(claimant_last_name)) {
      missingFields.append("claimant_last_name, ");
    }
    if (isNullOrEmpty(claimant_email)) {
      missingFields.append("claimant_email, ");
    }
    if (isNullOrEmpty(claimant_phone)) {
      missingFields.append("claimant_phone, ");
    }
    if (isNullOrEmpty(accident_location)) {
      missingFields.append("accident_location, ");
    }
    if (isNullOrEmpty(accident_latitude)) {
      missingFields.append("accident_latitude, ");
    }
    if (isNullOrEmpty(accident_longitude)) {
      missingFields.append("accident_longitude, ");
    }
    if (isNullOrEmpty(policy_number)) {
      missingFields.append("policy_number, ");
    }
    if (isNullOrEmpty(fraud_score)) {
      missingFields.append("fraud_score, ");
    }

    if (missingFields.length() > 0) {
      return missingFields.toString();
    }
    return "";
  }

  private boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }
}
