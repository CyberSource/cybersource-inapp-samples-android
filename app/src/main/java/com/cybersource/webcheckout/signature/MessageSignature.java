package com.cybersource.webcheckout.signature;

import com.cybersource.inappsdk.datamodel.transaction.SDKTransactionObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by fzubair on 1/6/15.
 */
public class MessageSignature {

    private final static String SEPARATOR = "#";

    //-------WARNING!----------------
    // 1) Step 1 - Soap toolkit Transaction key should never be stored on the device or embedded in the code.
    // This part of the code that generates the Message-Signature is present here only to make the sample app work.
    // Message-Signature generation should be done on the server.
    String transactionSecretKey = "r1bngjB1sJIz6HrmFdBubAh46xMVHWwsjw66AzlHksJW3TrAnXNiuoWOjA21zv/7ipfk9L7dPXbQat09UiYcD6l8534vVMKzlL0+os2kFHA1nwgOIct8f6PLJqGw+FnXvYPDllNrFgvtXELoP8JzTDP4HwmWA7XRp4tC0Bsq3pKP1w1iXj2CTXV3T+FtL4nXC3XxkDEVQN8jTTBGniQOGfMlFoVkzkhjOPwMy94fUVS0/PwSZit12gqPJ7/xE07oC5+EV+XGEjXZuEaKtSNJECN51qkcKHpikdaXKJT42oXOFLGLgh/SbcJtgjVWRarbf/P9E27lzuIS8pNX9HKJdQ==";
    private static MessageSignature instance = null;

    public static final MessageSignature getInstance() {
        if (instance == null) {
            instance = new MessageSignature();
        }
        return instance;
    }

    /**
     * Covers all the steps to create a Message-Signature
     *
     * @param transactionObject
     * @param loginId
     * @return
     *
     */
    public String generateSignature(SDKTransactionObject transactionObject, String loginId){
        String stringHmacSha1 = null;
        try {
            // Take HMAC-SHA1 of the transaction-key
            stringHmacSha1 = getHmacSha1(transactionSecretKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Get the amount of the transaction (amount only needed for ICS_AUTH service but not for ICS_ENCRYPT_SERVICE)
        String amount = getGatewayAmountString(BigDecimal.ZERO);

        // Get the current timeStamp in UTC format.
        String timeStamp = getCurrentTimestampUTC();

        // Concatenate all together into a single string:
        String signatureComponents = stringHmacSha1
                + loginId                                   // 3) Step 3 - Get your API_LOGIN_ID
                + transactionObject.getMerchantReferenceCode()    // 4) Get the merchantReferenceCode
                /*+ amount*/ + timeStamp;
        String hashedSignatureComponents = null;
        try {
            // Take HMAC-SHA256 of the signatureComponents
            hashedSignatureComponents = getHmacSHA256(signatureComponents);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Finally create the final signature:
        String finalSignature = hashedSignatureComponents + SEPARATOR + timeStamp;
        return finalSignature;
    }

    /**
     * Simply rounds up the value to be used over at the gateway. Takes care of different or long
     * lengths of amount
     *
     * @param value
     * @return
     */
    public String getGatewayAmountString(BigDecimal value){
        BigDecimal amount = value.setScale(2, RoundingMode.CEILING);
        return amount.toPlainString();
    }

    /**
     * Generates the HmacSha1 for the transactionKey
     *
     * @param soapToolkitTransactionKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static String getHmacSha1(String soapToolkitTransactionKey) throws
            NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(soapToolkitTransactionKey.getBytes("UTF-8"));
        byte[] bytes = crypt.digest();
        StringBuffer hash = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
    }

    /**
     * Generates the HmacSha256 for the figurePrintComponents
     *
     * @param inputStringForPassword
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static String getHmacSHA256(String inputStringForPassword) throws
            InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String MAC_ALGORITHM_SHA256 = "HmacSHA256";
        SecretKeySpec key = new SecretKeySpec((inputStringForPassword).getBytes("UTF-8"),
                MAC_ALGORITHM_SHA256);
        Mac mac = Mac.getInstance(MAC_ALGORITHM_SHA256);
        mac.init(key);
        byte[] bytes = mac.doFinal(inputStringForPassword.getBytes("ASCII"));
        StringBuffer hash = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
    }

    /**
     * Generates a timeStamp to be used for the signature generation.
     * The value is String representation of the current date&time in UTC
     */
    private static String getCurrentTimestampUTC() {
        Calendar requestDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestampUTC = dateFormat.format(requestDateTime.getTime());
        return timestampUTC;
    }

}
