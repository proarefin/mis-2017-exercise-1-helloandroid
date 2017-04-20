package com.example.arefin.myfrstapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivitty extends AppCompatActivity {

    private static final String DEBUG_TAG = "HttpExample";

    private EditText urlText;
    private TextView textView;
    private TextView txtLabel;
    private ProgressBar spinner;
    private boolean iswedChecked = false;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activitty);

        Button button = (Button) findViewById(R.id.btnPress);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
        }

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    urlText = (EditText) findViewById(R.id.txtUrl);

                    txtLabel = (TextView) findViewById(R.id.txtViewLbl);
                    textView = (TextView) findViewById(R.id.txtViewResponse);
                    txtLabel.setText("Hello ! User");
                    spinner.setVisibility(View.VISIBLE);
                    RadioButton rbtnTextview = (RadioButton) findViewById(R.id.rdbPlaintext);
                    webView = (WebView) findViewById(R.id.webView);
                    iswedChecked = rbtnTextview.isChecked();
                    //EditText txt = (EditText)findViewById(R.id.txtUrl);
                    //String str = txt.getText().toString();
                    if (!Patterns.WEB_URL.matcher(urlText.toString()).matches()) {
                        try {
                            // Before attempting to fetch the URL, makes sure that there is a network connection.
                            // Gets the URL from the UI's text field.
                            String stringUrl = urlText.getText().toString();
                            if (iswedChecked) {
                                ConnectivityManager connMgr = (ConnectivityManager)
                                        getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    spinner.setVisibility(View.GONE);
                                    new DownloadWebpageTask().execute(stringUrl);
                                } else {
                                    textView.setText("No network connection available.");
                                }
                                webView.setVisibility(View.INVISIBLE);
                                textView.setVisibility(View.VISIBLE);
                            } else {
                                spinner.setVisibility(View.GONE);
                                webView.setWebViewClient(new WebViewClient());
                                webView.loadUrl(stringUrl);
                                webView.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Invalid URL" + e, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_LONG);

                        toast.show();
                    }


                }
            });
        }


    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    public void connect(String rl) throws IOException {

        URL url = new URL(rl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();


            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            textView.setText(result);

        }
    }
}



