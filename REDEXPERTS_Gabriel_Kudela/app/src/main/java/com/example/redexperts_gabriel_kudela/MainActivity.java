package com.example.redexperts_gabriel_kudela;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends Activity {

    ProgressDialog pd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        (findViewById(R.id.downloadJSONButton)).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    downloadJSON("https://dl.dropboxusercontent.com/u/6556265/test.json");
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please check your internet connection.", Toast.LENGTH_LONG);
                    toast.show();
               }
            }
        });

        (findViewById(R.id.parseButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                try {
                    String tmp = ((EditText) findViewById(R.id.JSON_editView)).getText().toString();
                    if (!tmp.isEmpty()) {
                        parseButtonMethod(new JSONObject(tmp));
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "You should download json data first.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        (findViewById(R.id.showLocationButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!((EditText) findViewById(R.id.JSON_latitudeView)).getText().toString().isEmpty() && !((EditText) findViewById(R.id.JSON_longitudeView)).getText().toString().isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                    intent.putExtra("latitude", ((EditText) findViewById(R.id.JSON_latitudeView)).getText().toString());
                    intent.putExtra("longitude", ((EditText) findViewById(R.id.JSON_longitudeView)).getText().toString());
                    intent.putExtra("imageURL", ((EditText) findViewById(R.id.JSON_imageView)).getText().toString());
                    intent.putExtra("text", ((EditText) findViewById(R.id.JSON_textView)).getText().toString());
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please parse JSON first, to get latitude and longitude.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }



    private void downloadJSON (String url) {
        JSONDownloader jd = new JSONDownloader();
            jd.execute(url);
    }

    private void parseButtonMethod(JSONObject json) {

        JSONObject locationObject = null;
        try {
            locationObject = json.getJSONObject("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert locationObject != null;
        ((EditText) findViewById(R.id.JSON_latitudeView)).setText(locationObject.optString("latitude"));
        ((EditText) findViewById(R.id.JSON_longitudeView)).setText(locationObject.optString("longitude"));
        ((EditText) findViewById(R.id.JSON_textView)).setText(json.optString("text"));
        ((EditText) findViewById(R.id.JSON_imageView)).setText(json.optString("image"));

    }

    private void updateData(String jsonString) {
        if (jsonString.equals("")) {
            ((EditText) findViewById(R.id.JSON_editView)).setText("Unable to download data.");
        } else {
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (json != null) {
                ((EditText) findViewById(R.id.JSON_editView)).setText(json.toString());
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Couldn\'t download JSON file", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
    public class JSONDownloader extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... params) {

            String url = params[0];

            return connect(url);
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
        public String connect(String url)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response;
            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    instream.close();
                    return result;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(MainActivity.this,"Progress Dialog","Downloading data",true,false);
        }

        @Override
        protected void onPostExecute(String s) {
            updateData(s);
            pd.dismiss();
        }
    }
}
