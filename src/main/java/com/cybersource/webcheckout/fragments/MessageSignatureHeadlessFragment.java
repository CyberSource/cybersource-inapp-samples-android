package com.cybersource.webcheckout.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybersource.inappsdk.connectors.inapp.InAppSDKApiClient;
import com.cybersource.inappsdk.connectors.inapp.receivers.TransactionResultReceiver;


public class MessageSignatureHeadlessFragment extends Fragment {

    public static final String TAG = "TransactionHeadlessFragment";
    public static final String ARG_RESULT_RECEIVER = "result_receiver";
    public static String API_LOGIN_ID = "cybs_lg_sa_merchant"; // replace with YOUR_API_LOGIN_ID
    public static String API_LOGIN_ID_NEW = "mpos_paymentech"; // replace with YOUR_API_LOGIN_ID
    private static String TRANSACT_NAMESPACE = "urn:schemas-cybersource-com:transaction-data-1.120";

    InAppSDKApiClient apiClient;
    private TransactionResultReceiver mReceiver;
    public MessageSignatureHeadlessFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mReceiver = getArguments().getParcelable(ARG_RESULT_RECEIVER);
        }
        setRetainInstance(true);
        startServiceWithReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    private void startServiceWithReceiver() {
/*        Intent msgIntent = new Intent(getActivity(), AppTransactionService.class);
        msgIntent.putExtra(AppTransactionService.SERVICE_ACTION_TAG, BaseActivity.PAYMENT_ACTION);
        msgIntent.putExtra(AppTransactionService.SERVICE_RESULT_RECEIVER, mReceiver);
        getActivity().startService(msgIntent);*/
    }
}
