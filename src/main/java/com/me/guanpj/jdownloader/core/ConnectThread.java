package com.me.guanpj.jdownloader.core;

import com.me.guanpj.jdownloader.utility.Constant;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jie on 2017/5/11.
 */

public class ConnectThread implements Runnable {

    private final String url;
    private final ConnectListener listener;
    private volatile boolean isRunning = false;

    public ConnectThread(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        boolean isSupportRange = false;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(Constant.READ_TIMEOUT);
            connection.setConnectTimeout(Constant.CONNECT_TIMEOUT);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                if("bytes".equals(connection.getHeaderField("Accept-Ranges"))) {
                    isSupportRange = true;
                }
                listener.onConnectSuccess(isSupportRange, contentLength);
            } else {
                listener.onConnectError("server error:" + responseCode);
            }
            isRunning = false;
        } catch (Exception e) {
            isRunning = false;
            listener.onConnectError(e.getMessage());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public interface ConnectListener{
        void onConnectSuccess(boolean isSupportRange, int totalLength);
        void onConnectError(String errorMessage);
    }
}
