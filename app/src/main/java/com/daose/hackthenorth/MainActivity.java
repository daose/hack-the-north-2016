package com.daose.hackthenorth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

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

    private void onYelpDetected() {
        //API: https://github.com/Yelp/yelp-android
        //Getting the business
        try {
            //setting the path
            Uri fullPath = Uri.parse("https://www.yelp.ca");
            String path = fullPath.getLastPathSegment();
            //Extracting data from Yelp
            Call<Business> call = yelpAPI.getBusiness(path);
            Response<Business> response = call.execute();
            //responses from Business
            Business business = response.body();
            String businessName = business.name();
            Double businessRating = business.rating();
            String businessAddress = business.location().toString();
            String businessImage = business.imageUrl();
            //Going to set objects in YelpBusiness
            YelpBusiness money = new YelpBusiness();
            money.setAddress(businessAddress);
            money.setName(businessName);
            money.setRating(businessRating);
            money.setImageUrl(businessImage);

        } catch (IOException e){
        }
        //Getting the business phone number
        try {
            //setting the path
            Uri fullPath = Uri.parse("https://www.yelp.ca");
            String path = fullPath.getLastPathSegment();
            //Extracting data from Yelp
            Call<SearchResponse> call = yelpAPI.getPhoneSearch(path);
            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();
            ArrayList<Business> businesses = searchResponse.businesses();
            String num = businesses.get(0).phone();
            //Setting the phone# in YelpBusiness
            YelpBusiness number = new YelpBusiness();
            number.setPhoneNum(num);
        }catch (Exception e){
        }
    }
    class YelpBusiness {
        private double rating;
        private String name;
        private String phoneNum;
        private String address;
        private String imageUrl;

        public YelpBusiness(/*double rating, String name, String phoneNum, String address*/){
            /*this.rating = rating;
            this.name = name;
            this.phoneNum = phoneNum;
            this.address = address;*/

        }
        public void setRating(double rating){
            this.rating = rating;
        }
        public void setName(String name){
            this.name = name;
        }
        public void setPhoneNum(String phoneNum){
            this.phoneNum = phoneNum;
        }
        public void setAddress(String address){
            this.address = address;
        }
        public void setImageUrl(String imageUrl){
            this.imageUrl = imageUrl;
        }
        public double getRating(){
            return this.rating;
        }
        public String getName(){
            return this.name;
        }
        public String getPhoneNum(){
            return this.phoneNum;
        }
        public String getAddress(){
            return this.address;
        }
        public String getImageUrl(){
            return this.imageUrl;
        }

    }

}
