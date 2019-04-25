package com.cybersource.webcheckout.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cybersource.webcheckout.R;
import com.cybersource.webcheckout.utils.GooglePay;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class MainFragment extends Fragment implements View.OnClickListener {

    private Button checkoutButton;
    private RelativeLayout googlePayButton;
    private EditText mAmountView;
    private TextView mGooglePayStatusText;
    private TextView mGooglePayToken;
    private static final String TAG_FRAGMENT_WEBCHECKOUT = "TAG_FRAGMENT_WEBCHECKOUT";

    /**
     * A client for interacting with the Google Pay API.
     */
    private PaymentsClient mPaymentsClient;

    /**
     * Arbitrarily-picked constant integer define to track a request for payment data activity.
     */
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    public MainFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPaymentsClient = GooglePay.createPaymentsClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mAmountView = (EditText) view.findViewById(R.id.amount_view);
        mGooglePayStatusText = (TextView) view.findViewById(R.id.google_pay_status);
        mGooglePayToken = (TextView) view.findViewById(R.id.google_pay_token);
        checkoutButton = (Button) view.findViewById(R.id.button_checkout);
        checkoutButton.setOnClickListener(this);
        googlePayButton = (RelativeLayout) view.findViewById(R.id.button_google_pay);
        googlePayButton.setOnClickListener(this);
        possiblyShowGooglePayButton();
        return view;
    }

    private void possiblyShowGooglePayButton() {
        final Optional<JSONObject> isReadyToPayJson = GooglePay.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        if (request == null) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    setGooglePayAvailable(task.getResult());
                } else {
                    mGooglePayStatusText.setText(getActivity().getString(R.string.is_ready_to_pay_failed));
                    Log.d("isReadyToPay failed", task.getException().getMessage());
                }
            }
        });
    }

    private void setGooglePayAvailable(boolean available) {
        if (available) {
            mGooglePayStatusText.setVisibility(View.GONE);
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            mGooglePayStatusText.setText(getActivity().getString(R.string.google_pay_unavailable));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_checkout:
                pullWebCheckoutFragment();
                break;

            case R.id.button_google_pay:
                requestPayment(view);
                break;
        }
    }

    private void pullWebCheckoutFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        WebCheckoutFragment webCheckoutFragment = (WebCheckoutFragment)
                fragmentManager.findFragmentByTag(TAG_FRAGMENT_WEBCHECKOUT);
        if (webCheckoutFragment == null) {
            webCheckoutFragment = new WebCheckoutFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.web_checkout_fragment_container, webCheckoutFragment, TAG_FRAGMENT_WEBCHECKOUT)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(View view) {
        if (mAmountView.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter amount.", Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(mAmountView.getText().toString()) <= 0) {
            Toast.makeText(getActivity(), "Please enter amount greater than zero.", Toast.LENGTH_SHORT).show();
        } else {

            // Disables the button to prevent multiple clicks.
            googlePayButton.setClickable(false);

            // The price provided to the API should include taxes and shipping.
            // This price is not displayed to the user.
            String price = mAmountView.getText().toString();
            Optional<JSONObject> paymentDataRequestJson = GooglePay.getPaymentDataRequest(price);
            if (!paymentDataRequestJson.isPresent()) {
                return;
            }
            PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

            // Since loadPaymentData may show the UI asking the user to select a payment method,
            // (with message " Unrecognised app. Please make sure that you trust this app before proceeding "
            // - This error indicates that the app is using ENVIRONMENT_TEST)
            // we use AutoResolveHelper to wait for the user interacting with it. Once completed,

            // onActivityResult will be called with the result.
            if (request != null) {
                AutoResolveHelper.resolveTask(
                        mPaymentsClient.loadPaymentData(request), getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                    default:
                        // Do nothing.
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        try {
            String paymentInformation = paymentData.toJson();
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            if (paymentInformation == null) {
                return;
            }
            // if using gateway tokenization, pass this token without modification
            JSONObject paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
            String paymentToken = paymentMethodData.getJSONObject("tokenizationData").getString("token");
            SpannableStringBuilder str = new SpannableStringBuilder("Google pay token:"+ " \n\n " + convertToBase64Encoding(paymentToken));
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mGooglePayToken.setText(str);
            mGooglePayToken.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(int statusCode) {
        Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    public static String convertToBase64Encoding(String str) {
        byte[] data = new byte[0];
        try {
            data = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
        return base64;
    }
}
