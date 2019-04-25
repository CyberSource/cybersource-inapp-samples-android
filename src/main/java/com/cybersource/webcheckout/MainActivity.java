package com.cybersource.webcheckout;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cybersource.webcheckout.fragments.MainFragment;
import com.cybersource.webcheckout.fragments.WebCheckoutFragment;
import com.cybersource.webcheckout.receivers.MessageSignatureResultReceiver;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements MessageSignatureResultReceiver.Receiver {

    private static final String TAG_FRAGMENT_MAIN = "TAG_FRAGMENT_MAIN";
    public static final String TAG_FRAGMENT_MESSGAGE_SIGNATURE_HEADLESS = "TAG_FRAGMENT_MESSGAGE_SIGNATURE_HEADLESS";

    private MessageSignatureResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullMainFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the result receiver to get the Message Signature back from the server
        registerResultReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void pullMainFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        MainFragment mainFragment = (MainFragment)
                fragmentManager.findFragmentByTag(TAG_FRAGMENT_MAIN);
        if(mainFragment == null) {
            mainFragment = new MainFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.web_checkout_fragment_container, mainFragment, TAG_FRAGMENT_MAIN)
                    .commit();
        }
    }

    private void registerResultReceiver() {
        if(resultReceiver != null)
            return;
        resultReceiver = new MessageSignatureResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
/*        switch (resultCode){
            case MessageSignatureService.SERVICE_RESULT_CODE_SDK_RESPONSE:
                SDKGatewayResponse response = (SDKGatewayResponse) resultData
                        .getParcelable(MessageSignatureService.SERVICE_RESULT_RESPONSE_KEY);
                break;
            case MessageSignatureService.SERVICE_RESULT_CODE_SDK_ERROR:
                SDKError error = (SDKError) resultData
                        .getSerializable(MessageSignatureService.SERVICE_RESULT_ERROR_KEY);
                break;
        }*/
    }

    //send the result to OnActivityResult defined in the fragments attached to the Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}
