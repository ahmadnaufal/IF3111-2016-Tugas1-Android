package com.ahmadnaufalfarhan.guestguessitb;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    /* Start the Google Maps Activity by getting the intent */
    public void requestChallengeHandler(View view) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connMgr.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isConnected())
            new RequestChallengeTask().execute(Constants.URL_PRODUCTION);
        else
            Toast.makeText(MainActivity.this, "Not connected to internet", Toast.LENGTH_SHORT).show();
    }

    /**
     *   This is the main class for our request forwarding method.
     *   We will be using AsyncTask to run our request on the background
     */
    private class RequestChallengeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder sb = new StringBuilder();

            HttpURLConnection conn = null;
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();

                /* connection properties */
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                /* create the request JSON object */
                JSONObject requestJson = new JSONObject();
                requestJson.put(Constants.PRM_COMMUNICATION, Constants.COM_REQLOCATION);
                requestJson.put(Constants.PRM_NIM, Constants.NIM_VALUE);

                conn.connect();     // connect to the url
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(requestJson.toString());      // write the request to outputstream, sending them to server
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // if the response is OK (200), get the response string
                    // and parse them as JSON
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line + "\n");

                    br.close();

                    return sb.toString();
                } else {
                    return conn.getResponseMessage();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error with the URL. Check all spellings!";
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Kucing", e.getMessage());
                return "Error with the IO.";
            } catch (JSONException e) {
                e.printStackTrace();
                return "Error with the JSON. Check all spellings!";
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    }


}
