package com.cybersource.webcheckout.utils;

import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;
import java.util.List;

public class GooglePayConstants {

    private GooglePayConstants() {
    }

    public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;
    public static final String PAYMENT_GATEWAY_NAME = "cybersource";
    public static final String PAYMENT_GATEWAY_MERCHANT_ID = "mpos_sdk_cas";
    public static final String CURRENCY_CODE = "USD";

    public static final List<String> SUPPORTED_NETWORKS = Arrays.asList(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA");

    public static final List<String> SUPPORTED_METHODS =
            Arrays.asList(
                    "PAN_ONLY",
                    "CRYPTOGRAM_3DS");

}
