package com.example.bottomactivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest {

    public static String executeHttpGetRequest(String targetURL) {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            InputStream iStream;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                iStream = connection.getErrorStream();
            }
            else {
                iStream = connection.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            String line;
            StringBuffer result = new StringBuffer();

            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append('\r');
            }
            br.close();

            return result.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
