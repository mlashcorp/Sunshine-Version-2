/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.spinitcloud.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements DashboardFragment.Callback {
    private class TransmitThread extends Thread {
        private String payload;
        public TransmitThread(String payload) {
            this.payload = payload;
        }

        private void sendBTData(String payload) {
            if (btSocket == null || !btSocket.isConnected()){
                Log.d("spinit-cloud", "no socket connected - establishing connection");
                //mConnectThread = new ConnectThread();
                //mConnectThread.start();
            }

            byte[] msgBuffer = payload.getBytes();
            try {
                //mConnectThread.join();
                outStream.write(msgBuffer);
            } catch (Exception e) {
                String msg = "In sendBTData and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
                msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                Log.d("Fatal Error", msg);
            }
        }

        public void run() {
            Log.d("spinit-cloud","Starting data transmission via thread!");
            this.sendBTData(this.payload);
        }

        public void cancel() {
            ;
        }
    }

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    // Bluetooth variables
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private static String address = "00:1A:7D:DA:71:04";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private ContactBTDevice mContactThread;
    private TransmitThread mTransmitThread;
    private boolean mTwoPane;

    private class ContactBTDevice extends AsyncTask<String,Integer,Integer> {

        private void establishConnection(){
            Log.d("cloud-debug","\n...In onResume...\n...Attempting client connect...");

            // Set up a pointer to the remote node using it's address.
            Log.d("spinit-cloud","1");
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            Log.d("spinit-cloud","2");
            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }
            Log.d("spinit-cloud","3");
            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();
            Log.d("spinit-cloud", "4");
            // Establish the connection.  This will block until it connects.
            try {
                btSocket.connect();
                Log.d("cloud-debug","\n...Connection established and data link opened...");
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    Log.d("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
            Log.d("spinit-cloud","5");
            // Create a data stream so we can talk to server.
            Log.d("cloud-debug","\n...Sending message to server...");

            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                Log.d("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }
        protected Integer doInBackground(String ... uids ) {

            return 0;
        }

        protected void onPostExecute(Integer result) {
            ;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.example.android.spinitcloud.app.R.layout.activity_main);
        if (findViewById(com.example.android.spinitcloud.app.R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(com.example.android.spinitcloud.app.R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        DashboardFragment dashboardFragment =  ((DashboardFragment)getSupportFragmentManager()
                .findFragmentById(com.example.android.spinitcloud.app.R.id.fragment_dashboard));
        dashboardFragment.setUseTodayLayout(false);
        ImageView img= (ImageView) findViewById(R.id.spinit_icon_image);
        img.setImageResource(R.mipmap.orange_icon);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.example.android.spinitcloud.app.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.example.android.spinitcloud.app.R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(com.example.android.spinitcloud.app.R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
