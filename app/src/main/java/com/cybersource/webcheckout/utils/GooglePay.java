package com.cybersource.webcheckout.utils;

import android.app.Activity;

import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class GooglePay {

    private GooglePay() {
    }

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    /**
     * Creates an instance of {@link PaymentsClient} for use in an {@link Activity} using the
     * environment and theme set in {@link GooglePayConstants}.
     *
     * @param activity is the caller's activity.
     */
    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(GooglePayConstants.PAYMENTS_ENVIRONMENT).build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    /**
     * Gateway Integration: Identify gateway and app's gateway merchant identifier.
     *
     * <p>The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     */
    private static JSONObject getTokenizationSpecification() throws JSONException {
        JSONObject tokenizationSpecification = new JSONObject();
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
        tokenizationSpecification.put("parameters", new JSONObject()
                .put("gateway", GooglePayConstants.PAYMENT_GATEWAY_NAME)
                .put("gatewayMerchantId", GooglePayConstants.PAYMENT_GATEWAY_MERCHANT_ID));

        return tokenizationSpecification;
    }

    /**
     * Card networks supported by the app and the gateway.
     *
     * @return Allowed card networks
     */
    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray(GooglePayConstants.SUPPORTED_NETWORKS);
    }

    /**
     * Card authentication methods supported by the app and the gateway.
     *
     * @return Allowed card authentication methods.
     */
    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray(GooglePayConstants.SUPPORTED_METHODS);
    }

    /**
     * Describe app's support for the CARD payment method.
     *
     * <p>The provided properties are applicable to both an IsReadyToPayRequest and a PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     */
    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", getAllowedCardNetworks());

        cardPaymentMethod.put("parameters", parameters);
        return cardPaymentMethod;
    }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     */
    private static JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getTokenizationSpecification());

        return cardPaymentMethod;
    }

    /**
     * JSONObject describing accepted forms of payment by the app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     */
    public static Optional<JSONObject> getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
            return Optional.of(isReadyToPayRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     */
    private static JSONObject getTransactionInfo(String price) throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", price);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", GooglePayConstants.CURRENCY_CODE);

        return transactionInfo;
    }

    /**
     * Information about the merchant requesting payment information
     *
     * @return Information about the merchant.
     * @throws JSONException
     */
    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", "Visa Inc.");
    }

    /**
     * JSONObject describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by the app.
     */
    public static Optional<JSONObject> getPaymentDataRequest(String price) {
        try {
            JSONObject paymentDataRequest = getBaseRequest();
            paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
            paymentDataRequest.put("transactionInfo", getTransactionInfo(price));
            paymentDataRequest.put("merchantInfo", getMerchantInfo());
            return Optional.of(paymentDataRequest);
        } catch (JSONException e) {
            return Optional.empty();
        }

    }
}
