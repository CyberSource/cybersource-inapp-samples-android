package com.cybersource.inapp.fragments.encryption;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.visa.inappsdk.common.SDKCurrency;
import com.visa.inappsdk.common.error.SDKError;
import com.visa.inappsdk.connectors.inapp.InAppSDKApiClient;
import com.visa.inappsdk.connectors.inapp.receivers.TransactionResultReceiver;
import com.visa.inappsdk.connectors.inapp.transaction.client.InAppTransaction;
import com.visa.inappsdk.connectors.inapp.transaction.client.InAppTransactionType;
import com.visa.inappsdk.datamodel.response.SDKGatewayResponse;
import com.visa.inappsdk.datamodel.transaction.callbacks.SDKApiConnectionCallback;
import com.visa.inappsdk.datamodel.transaction.fields.SDKBillTo;
import com.cybersource.inapp.R;
import com.cybersource.inapp.services.MessageSignatureService;
import com.cybersource.inapp.signature.MessageSignature;
import com.visa.inappsdk.datamodel.transaction.fields.SDKLineItem;
import com.visa.inappsdk.datamodel.transaction.fields.SDKPurchaseOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for a test Android Pay Transaction
 *
 * Created by fzubair on 11/19/2015.
 */
public class AndroidPayFragment extends Fragment implements View.OnClickListener, SDKApiConnectionCallback {

    public static final String TAG = "AndroidPayFragment";
    private final String CVV = "256";
    private final String POSTAL_CODE = "98001";
    public static String API_LOGIN_ID = "test_paymentech_001"; // replace with YOUR_API_LOGIN_ID
    private static String TRANSACT_NAMESPACE = "urn:schemas-cybersource-com:transaction-data-1.120";
    // Public SEC key
    public static final String PUBLIC_KEY_SEC = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEGswOubUOZchQkOJPt41PMduC4Ma5ldBTIhZUMINvuTuQOrWIl3GhLDEl/75hFoWRkp+d+KBism9+1LBBa2gBnw==";

    /** SOAP Payments TEST endpoint address. */
    public static String PAYMENTS_TEST_URL = "https://mobiletest.ic3.com/mpos/transactionProcessor/";
    /** SOAP Payments PROD endpoint address. */
    public static String PAYMENTS_PROD_URL = "https://mobile.ic3.com/mpos/transactionProcessor/";

    private Button checkoutButton;

    private ProgressDialog progressDialog;
    private RelativeLayout responseLayout;
    private TextView responseTitle;
    private TextView responseValue;

    InAppSDKApiClient apiClient;

    private TransactionResultReceiver resultReceiver;

    public AndroidPayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // build an InApp SDK Api client to make API calls.
        // parameters:
        // 1) Context - current context
        // 2) InAppSDKApiClient.Environment - CYBS ENVIRONMENT
        // 3) API_LOGIN_ID String - merchant's API LOGIN ID
        apiClient = new InAppSDKApiClient.Builder
                (getActivity(), InAppSDKApiClient.Environment.ENV_TEST, API_LOGIN_ID)
                .sdkConnectionCallback(this) // receive callbacks for connection results
                .sdkApiProdEndpoint(PAYMENTS_PROD_URL) // option to configure PROD Endpoint
                .sdkApiTestEndpoint(PAYMENTS_TEST_URL) // option to configure TEST Endpoint
                .publicKey(PUBLIC_KEY_SEC) // option to set the sec public key - needed for Android pay transaction
                .sdkConnectionTimeout(5000) // optional connection time out in milliseconds
                .transactionNamespace(TRANSACT_NAMESPACE) // optional - ApiClient has a default namespace too
                .build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_android_pay, container, false);

        checkoutButton = (Button) view.findViewById(R.id.button_checkout_order);
        checkoutButton.setOnClickListener(this);

        responseLayout =  (RelativeLayout) view.findViewById(R.id.response_layout);
        responseTitle =  (TextView) view.findViewById(R.id.encrypted_data_title);
        responseValue =  (TextView) view.findViewById(R.id.encrypted_data_view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // un-register the result receiver
        resultReceiver = null;
    }


    @Override
    public void onClick(View v) {
        progressDialog = ProgressDialog.show(getActivity(), "Please Wait",
                "Performing Android Pay Transaction...", true);
        if(responseLayout.getVisibility() == View.VISIBLE)
            responseLayout.setVisibility(View.GONE);

        InAppTransaction transactionObject = prepareAndroidPayTransactionObject();
        try {
            // make a call to connect to API
            // parameters:
            // 1) InAppSDKApiClient.Api - Type of API to make a request
            // 2) SDKTransactionObject - The transactionObject for the current transaction
            // 3) Signature String - fresh message signature for this transaction
            apiClient.performApi(InAppSDKApiClient.Api.API_ANDROID_PAY,
                    transactionObject, generateSignature(transactionObject));
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private SDKBillTo prepareBillingInformation(){
        SDKBillTo billTo = new SDKBillTo.Builder("First Name", "Last Name")
                .street1("8397 158th AVE NE")
                .city("Redmond")
                .state("WA")
                .postalCode("98052")
                .country("United States")
                .email("fzubair@visa.com")
                .build();
        return billTo;
    }

    /**
     * prepares a transaction object to be used with the Gateway Android Pay transaction
     */
    private InAppTransaction prepareAndroidPayTransactionObject() {
        // create a transaction object by calling the predefined api for creation
        return InAppTransaction
                // type of transaction object
                .createTransactionObject(InAppTransactionType.IN_APP_TRANSACTION_ANDROID_PAY)
                // can be set to anything meaningful
                .merchantReferenceCode("Android_InApp_Sample_Code" + "_" + Long.toString(System.currentTimeMillis()))
                // billing information
                .billTo(prepareBillingInformation())
                // paymentMethodToken String returned buy the FullWallet.getPaymentMethodToken()
                .encryptedPaymentData("eyJlcGhlbWVyYWxQdWJsaWNLZXkiOiJCQmROQUdJdmVpUmlDdWJ6ZHhNZHhqNVhSN0FZRjdyWkhGT1gyYmd3enB0XC9pUE41ZHlXbGJWcFVick5OZG93aE1FQnVXWEVUVzBvMHFHOWlQM3BmVVlzPSIsImVuY3J5cHRlZE1lc3NhZ2UiOiJ5SXNSOVRiRkxmckoxTHh1a0Y1eDNqWmFPYkJydE96T3ZmRXMzWExkdmdGa0ZrVVFLWVFIRFI3MHdQVEdKTlwvTHJNU1ZLdEhkYVdoMitlT3d5ZHhTS2Y0XC9OMWJmeERUWEM4a1pGYkx6TklyUUpSUU02MGtxZHNTREh6SGFJXC9tRTBTVUladTIyYmcrUUxNeEk4ZTRWSmhScWVobndhdzh6cDl3UGQ3eHJcL0ZtMHJreGR6XC9PSnJ6N0p2RUdTTU1wdHhmNTVMUVEzblJCdzJIb0dEb2QrIiwidGFnIjoiczd1bW83ZWh3SlwvbEFtdlhPbjlLNnYrbDBVY0NMOTdmZkM5OVFLc1dlRE09In0=")
                // the purchase order items and amount information
                .purchaseOrder(preparePurchaseOrder())
                .build();
    }

    private SDKPurchaseOrder preparePurchaseOrder(){
        SDKPurchaseOrder purchaseOrder = new SDKPurchaseOrder.Builder()
                .currency(SDKCurrency.USD)
                .items(prepareLineItems())
                .build();
        return purchaseOrder;
    }

    private List<SDKLineItem> prepareLineItems(){
        List<SDKLineItem> lineItems = new ArrayList<>();
        lineItems.add(new SDKLineItem.Builder
                ("Item one", new BigDecimal("0.04"), 2)
                .taxAmount(new BigDecimal("0.01"))
                .build());
        lineItems.add(new SDKLineItem.Builder
                ("Item two", new BigDecimal("0.55"), 1)
                .taxAmount(new BigDecimal("0.05"))
                .build());
        return lineItems;
    }

    /**
     * Generates the signature in the sdk to be used with the next call
     */
    private String generateSignature(InAppTransaction transactionObject) {
        return MessageSignature.getInstance().generateSignature
                (transactionObject, API_LOGIN_ID);
    }

    @Override
    public void onErrorReceived(SDKError error) {
        if(responseLayout.getVisibility() != View.VISIBLE)
            responseLayout.setVisibility(View.VISIBLE);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        responseTitle.setText(R.string.error);
        responseValue.setText(getString(R.string.code) + error.getErrorCode()
                + "\n" +getString(R.string.message) + error.getErrorMessage().toString()
                + "\n" +getString(R.string.extra_message) + error.getErrorExtraMessage().toString());
        Log.d(TAG, "onErrorReceived > " + error.getErrorExtraMessage().toString());
    }

    @Override
    public void onApiConnectionFinished(SDKGatewayResponse response) {
        if(responseLayout.getVisibility() != View.VISIBLE)
            responseLayout.setVisibility(View.VISIBLE);
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        responseTitle.setText(R.string.encrypted_card_data);
        responseValue.setText(getString(R.string.decision) + response.getDecision()
                + "\n" +getString(R.string.encrypted_data) + response.getEncryptedPaymentData());
        Log.d(TAG, "onApiConnectionFinished > " + response.getDecision());
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode){
            case MessageSignatureService.SERVICE_RESULT_CODE_SDK_RESPONSE:
                SDKGatewayResponse response = (SDKGatewayResponse) resultData
                        .getParcelable(MessageSignatureService.SERVICE_RESULT_RESPONSE_KEY);
                break;
            case MessageSignatureService.SERVICE_RESULT_CODE_SDK_ERROR:
                SDKError error = (SDKError) resultData
                        .getSerializable(MessageSignatureService.SERVICE_RESULT_ERROR_KEY);
                break;
        }
    }
}
