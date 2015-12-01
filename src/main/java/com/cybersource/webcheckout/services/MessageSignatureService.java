package com.cybersource.webcheckout.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Xml;

import com.cybersource.inappsdk.common.error.SDKError;
import com.cybersource.inappsdk.common.error.SDKGatewayError;
import com.cybersource.inappsdk.common.error.SDKInternalError;
import com.cybersource.inappsdk.common.utils.SDKUtils;
import com.cybersource.inappsdk.connectors.inapp.connection.InAppConnectionData;
import com.cybersource.inappsdk.connectors.inapp.envelopes.InAppBaseEnvelope;
import com.cybersource.inappsdk.connectors.inapp.receivers.TransactionResultReceiver;
import com.cybersource.inappsdk.connectors.inapp.responses.InAppResponseObject;
import com.cybersource.inappsdk.datamodel.SDKGatewayErrorMapping;
import com.cybersource.inappsdk.datamodel.response.SDKGatewayResponse;
import com.cybersource.inappsdk.soap.connection.SDKConnectionConstants;
import com.cybersource.inappsdk.soap.parser.SDKSoapParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

/**
 * Handling asynchronous task requests in
 * a service on a separate handler thread for CyberSource Gateway
 * <p/>
 *
 * Created by fzubair on 10/16/2015.
 */
public class MessageSignatureService extends IntentService {

    /** Reason code: 100 */
    private static String REASON_CODE_OK = "100";
    /** Reason code: 120 */
    private static String REASON_CODE_DISCOUNTED_OK = "120";
    /** Reason code: 101 */
    private static String REASON_CODE_MISSING_FIELD = "101";
    /** Reason code: 102 */
    private static String REASON_CODE_INVALID_FIELD = "102";
    /** Reason code: 110 */
    private static String REASON_CODE_PARTIAL = "110";
    private final static String POST = "POST";

    public static final String SERVICE_ACTION_MESSAGE_SIGNATURE = "com.visa.inappsdk.connectors.inapp.connection.action.MESSAGE_SIGNATURE";
    public static final String SERVICE_ACTION_TRANSACTION = "com.visa.inappsdk.connectors.inapp.connection.action.tRANSACTION";

    private static final String EXTRA_PARAM_CONNECTION_RESULT_RECEIVER = "com.visa.inappsdk.connectors.inapp.connection.extra.RESULT_RECEIVER";

    public static final String SERVICE_RESULT_RESPONSE_KEY = "SERVICE_RESULT_RESPONSE_KEY";
    public static final String SERVICE_RESULT_ERROR_KEY = "SERVICE_RESULT_ERROR_KEY";
    public static final int SERVICE_RESULT_CODE_SDK_RESPONSE = 100;
    public static final int SERVICE_RESULT_CODE_SDK_ERROR = 200;

    /**
     * Starts this service to perform action CONNECT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param envelope - Envelope that will be send to Gateway
     * @param resultReceiver - result receiver to notify the gateway when the service has a result
     *
     * @see IntentService
     */
    public static void startActionConnect(Context context, final InAppBaseEnvelope envelope, TransactionResultReceiver resultReceiver) {
        Intent intent = new Intent(context, MessageSignatureService.class);
        intent.setAction(SERVICE_ACTION_TRANSACTION);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PARAM_CONNECTION_RESULT_RECEIVER, resultReceiver);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public MessageSignatureService() {
        super("InAppConnectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            switch (action){
                case SERVICE_ACTION_TRANSACTION:
                    final ResultReceiver resultReceiver =
                            intent.getParcelableExtra(EXTRA_PARAM_CONNECTION_RESULT_RECEIVER);
                    Object result = handleActionConnect(null);
                    onPostHandleAction(result, resultReceiver);
                    break;
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private Object handleActionConnect(InAppBaseEnvelope envelope) {
        Object resultObject = null;
        String url = InAppConnectionData.PAYMENTS_CURRENT_URL;
        try {

            HttpsURLConnection urlConnection = SDKUtils.getHttpsURLConnection(url, POST, true);
            urlConnection.setRequestProperty(SDKConnectionConstants.CONTENT_TYPE_LABEL,
                    SDKConnectionConstants.XML_CONTENT_TYPE_PREFIX + envelope.getEncoding());
            urlConnection.setRequestProperty(SDKConnectionConstants.HEADER_KEY_SOAP_ACTION,
                    InAppBaseEnvelope.DEFAULT_SOAP_ACTION);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Xml.Encoding.UTF_8.name()));
            writer.write(SDKSoapParser.parseEnvelope(envelope));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InAppResponseObject result = envelope.parseResponse(urlConnection.getInputStream());
                if (result.reasonCode.equals(REASON_CODE_OK) || result.reasonCode.equals(REASON_CODE_PARTIAL)
                        || result.reasonCode.equals(REASON_CODE_DISCOUNTED_OK)) { // -- Faizan -- Added this line since the server has a code
                    // for discounted successful transaction as: 120

                    SDKGatewayResponse response = null;
                    resultObject = response;

                } else {
                    SDKGatewayError error = SDKGatewayErrorMapping.getGatewayError(result.reasonCode);
                    if (result.reasonCode.equals(REASON_CODE_MISSING_FIELD)) {
                        error.setErrorExtraMessage(result.missingField);
                    } else {
                        error.setErrorExtraMessage(result.icsMessage.icsRmsg);
                    }
                    resultObject = error;
                }
            } else if (responseCode == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                SDKError error = envelope.parseGatewayError(urlConnection.getErrorStream());
                resultObject = error;
            } else {
                SDKError error = SDKInternalError.SDK_INTERNAL_ERROR_NETWORK_CONNECTION;
                error.setErrorExtraMessage(String.valueOf(responseCode));
                resultObject = error;
            }
        } catch (IOException e) {
            e.printStackTrace();
            SDKError error = SDKInternalError.SDK_INTERNAL_ERROR_NETWORK_CONNECTION;
            error.setErrorExtraMessage(e.getMessage());
            resultObject = error;
        }
        return resultObject;
    }

    protected void onPostHandleAction(Object result, ResultReceiver resultReceiver) {
        Bundle resultData = new Bundle();
        if (result instanceof SDKGatewayResponse) {
            SDKGatewayResponse response = (SDKGatewayResponse)result;
            resultData.putParcelable(SERVICE_RESULT_RESPONSE_KEY, response);
            switch (response.getType()) {
                case SDK_ENCRYPTION:
                    resultReceiver.send(SERVICE_RESULT_CODE_SDK_RESPONSE, resultData);
                    break;
                default:
                    break;
            }
        } else if (result instanceof SDKError) {
            SDKError response = (SDKError)result;
            resultData.putSerializable(SERVICE_RESULT_ERROR_KEY, response);
            resultReceiver.send(SERVICE_RESULT_CODE_SDK_ERROR, resultData);
        }
    }
}
