package com.daose.hackthenorth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daose.hackthenorth.adapter.HoursAdapter;
import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YelpActivity extends AppCompatActivity {
    private static final String TAG = YelpActivity.class.getSimpleName();

    private static final String CONSUMER_KEY = "_pSsOl9dQIh4898P-kUoig";
    private static final String CONSUMER_SECRET = "ZNGrKazE9JEN2qxenYAPf2Rj0iM";
    private static final String TOKEN = "NrQ-3hTN62cPgeItv8b5otNIJiTkkpQ6";
    private static final String TOKEN_SECRET = "36y1ayki63KnTocDWrS-tZfWqXk";

    private YelpBusiness store;
    private YelpAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp);

        setupYelp(getIntent().getStringExtra("url"));
    }

    private void initUI() {
        ImageView background = (ImageView) findViewById(R.id.background);
        Picasso.with(this).load("https://s3-media2.fl.yelpcdn.com/assets/srv0/seo_metadata/e98ed5a1460f/assets/img/logos/yelp_og_image.png")
                .transform(new BlurTransformation(this))
                .fit()
                .centerCrop()
                .into(background);

        ImageView icon = (ImageView) findViewById(R.id.icon);
        Picasso.with(this).load(store.getImageUrl())
                .fit()
                .centerCrop()
                .into(icon);

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(store.getName());

        RatingBar stars = (RatingBar) findViewById(R.id.stars);
        stars.setRating((float) store.getRating());
        Drawable drawable = stars.getProgressDrawable();
        drawable.setColorFilter(Color.parseColor("#f44336"), PorterDuff.Mode.SRC_ATOP);

        ImageButton callButton = (ImageButton) findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + store.getPhoneNum()));
                startActivity(callIntent);
            }
        });

        ImageButton mapButton = (ImageButton) findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                mapIntent.setData(Uri.parse("google.navigation:q=" + store.getLatitude() + "," + store.getLongitude()));
                startActivity(mapIntent);
            }
        });
        RecyclerView hoursView = (RecyclerView) findViewById(R.id.hours_view);
        if (store.getHours()[0] == null || store.getHours()[0].isEmpty()) {
            hoursView.setVisibility(View.GONE);
        } else {
            hoursView.setLayoutManager(new LinearLayoutManager(this));
            hoursView.setAdapter(new HoursAdapter(store.getHours()));
        }
    }


    private void setupYelp(String url) {
        YelpAPIFactory apiFactory = new YelpAPIFactory(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        api = apiFactory.createAPI();
        store = new YelpBusiness();

        new GetHoursOfOperation().execute(url);
    }

    class YelpBusiness {
        private double rating;
        private String name;
        private String phoneNum;
        private String address;
        private String imageUrl;
        private String[] hours;

        private double latitude, longitude;

        public YelpBusiness() {
            hours = new String[7];
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String[] getHours() {
            return hours;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getRating() {
            return this.rating;
        }

        public String getName() {
            return this.name;
        }

        public String getPhoneNum() {
            return this.phoneNum;
        }

        public String getAddress() {
            return this.address;
        }

        public String getImageUrl() {
            return this.imageUrl;
        }

        @Override
        public String toString() {
            return name + " " + phoneNum + " " + address + " " + imageUrl + " " + rating;
        }

    }

    private class GetHoursOfOperation extends AsyncTask<String, Void, Void> {
        private Uri uri;

        @Override
        public Void doInBackground(String... params) {
            try {
                uri = Uri.parse(params[0]);
                Document doc = Jsoup.connect(uri.toString()).userAgent("Mozilla/5.0").get();

                Elements elements = doc.select("div.biz-hours td:not(td.extra)");
                if (elements == null || elements.isEmpty()) return null;
                for (int i = 0; i < elements.size(); i++) {
                    Log.d(TAG, "hour: " + elements.get(i).text());
                    store.getHours()[i] = elements.get(i).text();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void param) {
            String id = uri.getLastPathSegment();
            Log.d(TAG, "Business id: " + id);

            Call<Business> call = api.getBusiness(id);
            call.enqueue(new Callback<Business>() {
                @Override
                public void onResponse(Call<Business> call, Response<Business> response) {
                    Business business = response.body();
                    store.setName(business.name());
                    store.setPhoneNum(business.displayPhone());
                    store.setAddress(business.location().toString());
                    store.setLatitude(business.location().coordinate().latitude());
                    store.setLongitude(business.location().coordinate().longitude());
                    store.setImageUrl(business.imageUrl());
                    store.setRating(business.rating());
                    Log.d(TAG, "store: " + store.toString());

                    initUI();
                }

                @Override
                public void onFailure(Call<Business> call, Throwable t) {

                }
            });
        }
    }

}
