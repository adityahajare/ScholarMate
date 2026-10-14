package com.scholarmate.dto;
public record ProfileAddressRequest(String addressType, String line1, String line2, String state, String district, String taluka, String village, String pinCode, Boolean domicile) { }
