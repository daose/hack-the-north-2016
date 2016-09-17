package com.daose.hackthenorth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    /*
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    private static final String CONSUMER_KEY = "_pSsOl9dQIh4898P-kUoig";
    private static final String CONSUMER_SECRET = "ZNGrKazE9JEN2qxenYAPf2Rj0iM";
    private static final String TOKEN = "NrQ-3hTN62cPgeItv8b5otNIJiTkkpQ6";
    private static final String TOKEN_SECRET = "36y1ayki63KnTocDWrS-tZfWqXk";

    private YelpAPIFactory apiFactory;
    private YelpAPI yelpAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, UClipService.class);
        startService(intent);

        setupYelp();
    }

    private void setupYelp() {
        YelpAPIFactory apiFactory = new YelpAPIFactory(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();
    }
}
