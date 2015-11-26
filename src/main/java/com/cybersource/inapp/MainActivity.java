package com.cybersource.inapp;

import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cybersource.inapp.adapters.ViewPagerAdapter;
import com.cybersource.inapp.fragments.encryption.AndroidPayFragment;
import com.cybersource.inapp.fragments.encryption.EncryptionFragment;
import com.cybersource.inapp.receivers.MessageSignatureResultReceiver;


public class MainActivity extends AppCompatActivity implements MessageSignatureResultReceiver.Receiver {

    private static final String TAG_FRAGMENT_ENCRYPTION = "TAG_FRAGMENT_ENCRYPTION";
    private static final String TAG_FRAGMENT_ANDROID_PAY = "TAG_FRAGMENT_ANDROID_PAY";
    public static final String TAG_FRAGMENT_MESSGAGE_SIGNATURE_HEADLESS = "TAG_FRAGMENT_MESSGAGE_SIGNATURE_HEADLESS";

    private MessageSignatureResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
    }

    private void setupViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(pullEncryptionFragment(), "ENCRYPTION");
        adapter.addFrag(pullAndroidPayFragment(), "ANDROID PAY");
        viewPager.setAdapter(adapter);
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

    private Fragment pullEncryptionFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        EncryptionFragment webCheckoutFragment = (EncryptionFragment)
                fragmentManager.findFragmentByTag(TAG_FRAGMENT_ENCRYPTION);
        if(webCheckoutFragment == null) {
            webCheckoutFragment = new EncryptionFragment();
/*            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, webCheckoutFragment, TAG_FRAGMENT_ENCRYPTION)
                    .commit();*/
        }
        return webCheckoutFragment;
    }

    private Fragment pullAndroidPayFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        AndroidPayFragment androidPayFragment = (AndroidPayFragment)
                fragmentManager.findFragmentByTag(TAG_FRAGMENT_ANDROID_PAY);
        if(androidPayFragment == null) {
            androidPayFragment = new AndroidPayFragment();
/*            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, androidPayFragment, TAG_FRAGMENT_ANDROID_PAY)
                    .commit();*/
        }

        return androidPayFragment;
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
}
