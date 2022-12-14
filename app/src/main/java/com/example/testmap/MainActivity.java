package com.example.testmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private MapView BaiduMap;
    private LocationClient Client;
    private com.baidu.mapapi.map.BaiduMap baiduMap;//??????????????????
    private boolean isFirstLocate = true;
    boolean isRing = false;
    boolean isSleep = false;
    boolean isLoct = false;
    boolean isDist = false;
    int count = 0;
    int Count = 0;
    int RSSI_Value = 0;
    double Distance = 0;
    Socket socket = null;//??????socket
    Button ConnectBtn;//????????????
    Button RingBtn;//??????????????????
    Button sendBtn;//??????????????????
    Button SleepBtn;//????????????
    Button DistBtn;//????????????
    Button LoctBtn;//??????
    String IPEditText;//??????ip?????????
    EditText PortText;//?????????????????????
    private TextView DistTV;//????????????
    private InputStream inputStream = null;
    private WifiManager wifiManager;
    private WifiInfo info;
    private Timer timer,timer2;
    private TimerTask task;
    Marker marker = null;
    double[] x = {23.104593,23.104593,23.104593};
    double[] y = {113.303729,113.303729,113.303729};
    double[] d = {0.001,0.001,0.001};
    double x_0,x_1,x_2;
    double y_0,y_1,y_2;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String info= (String) msg.obj;
            if (msg.what == 1) {
                switch(msg.arg1) {
                    case 1:
                        Toast.makeText(getApplicationContext(), "???????????????",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "???????????????",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(getApplicationContext(), "???????????????",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(getApplicationContext(), "????????????????????????",
                                Toast.LENGTH_SHORT).show();
                        SleepBtn.setText("??????");
                        break;
                    case 5:
                        Toast.makeText(getApplicationContext(), "???????????????????????????",
                                Toast.LENGTH_SHORT).show();
                        SleepBtn.setText("????????????");
                        break;
                    case 6:
                        Toast.makeText(getApplicationContext(), "?????????????????????",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(getApplicationContext(), "???????????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(getApplicationContext(), "????????????",
                                Toast.LENGTH_SHORT).show();
                        ConnectBtn.setText("?????????");
                        ConnectBtn.setEnabled(false);
                        break;
                    default:
                        break;
                }
            }
            else if (msg.what == 2) {
                Distance = Math.pow(10,-0.0396*msg.arg1-2.2444);
                if (Distance < 30) {
                    DistTV.setText( "???????????????"+String.format("%.2f", Distance)+"???");
                } else {
                    DistTV.setText( "Wi-Fi?????????????????????????????????????????????");
                }
                if (isLoct) {
                    BDLocation location = new BDLocation();
                    x[count] = location.getLatitude();
                    y[count] = location.getLongitude();
                    d[count] = Distance;

                    x_0 = x[0]+(x[1]-x[0])*d[0]/(d[0]+d[1]);
                    x_1 = x[1]+(x[2]-x[1])*d[1]/(d[1]+d[2]);
                    x_2 = x[2]+(x[0]-x[2])*d[2]/(d[2]+d[0]);
                    y_0 = y[0]+(y[1]-y[0])*d[0]/(d[0]+d[1]);
                    y_1 = y[1]+(y[2]-y[1])*d[1]/(d[1]+d[2]);
                    y_2 = y[2]+(y[0]-y[2])*d[2]/(d[2]+d[0]);

                    double Lat = (x_0+x_1+x_2)/3;
                    double Lon = (y_0+y_1+y_2)/3;
                    LatLng point = new LatLng(Lat,Lon);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer20);
//??????MarkerOption???????????????????????????Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
//??????????????????Marker????????????
                    marker = (Marker) baiduMap.addOverlay(option);
                    delay(500);
                    marker.remove();
                    count++;
                    if (count >=3) {
                        count = 0;
                    }
                }
            }
        }
    };

    //????????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }
    //???????????????
    @SuppressLint("ResourceType")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.navigation_home:
                return super.onOptionsItemSelected(item);
            //return true;
            case R.id.navigation_dashboard:
                Intent intent=new Intent(MainActivity.this,Emermessage.class);
                startActivity(intent);
                return true;
            case R.id.navigation_notifications:
                AlertDialog dialog = new AlertDialog.Builder(this).create();//???????????????
                dialog.setTitle("Q&A");//?????????????????????
                dialog.setMessage("??????????????????????????????????????????????????????????????????\n??????????????????????????????????????????????????????\n???????????????????????????????????????");//????????????????????????
                //????????????2???button
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//???????????????
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//???????????????
                    }
                });
                dialog.show();//???????????????
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //????????????setContentView???????????????
        SDKInitializer.initialize(getApplicationContext());
        Client = new LocationClient(getApplicationContext());//????????????Context
        Client.registerLocationListener((BDLocationListener) new MyLocationListener());//???????????????????????????????????????????????????????????????????????????
        setContentView(R.layout.activity_main);
        BaiduMap = (MapView) findViewById(R.id.baiduMap);
        baiduMap = BaiduMap.getMap();//??????????????????????????????????????????????????????????????????????????????????????????
        baiduMap.setMyLocationEnabled(true);//????????????????????????????????????
        requestLocation();

        ConnectBtn = (Button) findViewById(R.id.Connect_Bt);//??????
        RingBtn = (Button) findViewById(R.id.Ring_Bt);//????????????
        SleepBtn = (Button) findViewById(R.id.Sleep_Bt);//????????????
        DistBtn = (Button) findViewById(R.id.Dist_Bt);//????????????
        sendBtn = (Button) findViewById(R.id.btnSendSMS);//????????????
        LoctBtn = (Button) findViewById(R.id.Loct_Bt);
        ConnectBtn.setOnClickListener(new MyClick());
        RingBtn.setOnClickListener(new MyClick());
        SleepBtn.setOnClickListener(new MyClick());
        DistBtn.setOnClickListener(new MyClick());
        LoctBtn.setOnClickListener(new MyClick());
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMSMessage();
            }
        });
        IPEditText = "192.168.4.1";
        PortText = (EditText) findViewById(R.id.Port_ET);//?????????????????????????????????
        DistTV = (TextView) findViewById(R.id.Dist_TV);//?????????????????????
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        timer = new Timer();
        timer2 = new Timer();
        task = new TimerTask(){
            @Override
            public void run(){
                Message msg = new Message();
                info = wifiManager.getConnectionInfo();
                RSSI_Value = info.getRssi();
                msg.what = 2;
                msg.arg1 = RSSI_Value;
                handler.sendMessage(msg);
            }
        };
    }

    Connect_Thread Connect_Thread1;
    private class MyClick implements View.OnClickListener{
        public void onClick(View v){
            switch(v.getId()){
                case R.id.Connect_Bt:
                    Connect_Thread1 = new Connect_Thread();
                    Connect_Thread1.start();
                    break;
                case R.id.Ring_Bt:
                    if (socket == null) {
                        RingBtn.setText("?????????");
                    } else {
                        try {
                            if (!isRing) {
                                RingBtn.setText("????????????");
                                Ring_Thread Ring_Thread1 = new Ring_Thread();
                                Ring_Thread1.start();
                                isRing = true;
                            } else {
                                RingBtn.setText("????????????");
                                StpRing_Thread StpRing_Thread1 = new StpRing_Thread();
                                StpRing_Thread1.start();
                                isRing = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.Sleep_Bt:
                    if (socket == null) {
                        SleepBtn.setText("?????????");
                    } else {
                        try {
                            if (!isSleep) {
                                //SleepBtn.setText("??????");
                                Sleep_Thread Sleep_Thread1 = new Sleep_Thread();
                                Sleep_Thread1.start();
                                isSleep = true;
                            } else {
                                //SleepBtn.setText("????????????");
                                StpSleep_Thread StpSleep_Thread1 = new StpSleep_Thread();
                                StpSleep_Thread1.start();
                                isSleep = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.Dist_Bt:
                    if (socket == null) {
                        DistBtn.setText("?????????");
                    } else {
                        timer.scheduleAtFixedRate(task, 200, 500);
                        DistBtn.setEnabled(false);
                        isDist = true;
                    }
                    break;
                case R.id.Loct_Bt:
                    if (socket == null) {
                        LoctBtn.setText("?????????");
                    } else {
                        timer.scheduleAtFixedRate(task, 200, 500);
                        isLoct = true;
                        DistBtn.setEnabled(false);
                        LoctBtn.setEnabled(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    class Connect_Thread extends Thread{
        public void run(){
            try {
                if (socket == null) {
                   InetAddress ipAddress = InetAddress.getByName(IPEditText);//???InetAddress????????????ip??????
                   int port = Integer.valueOf(PortText.getText().toString()) + 8000;//???????????????
                   socket = new Socket(ipAddress, port);//???????????????????????????
                   Receive_Thread Receive_Thread1 = new Receive_Thread();//???????????????????????????????????????
                   Receive_Thread1.start();
                   OutputStream outputStream1 = socket.getOutputStream();
                   outputStream1.write("A".getBytes());
                }
                else {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = 3;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Receive_Thread extends Thread {
        public void run() {
            while (true) {
                try {
                    final byte[] buf = new byte[1024];//?????????????????????
                    inputStream = socket.getInputStream();
                    final int len = inputStream.read(buf);//????????????????????????????????????
                    Message msg = new Message();
                    if (buf[0] == 'P') {
                        sendSMSMessage();
                    }
                    else {
                        msg.what = 1;
                        if (buf[0] == 'V') {
                            msg.arg1 = 1;
                        }
                        else if (buf[0] == 'D') {
                            msg.arg1 = 2;
                        }
                        else if (buf[0] == 'F') {
                            msg.arg1 = 4;
                        }
                        else if (buf[0] == 'R') {
                            msg.arg1 = 5;
                        }
                        else if (buf[0] == 'B') {
                            msg.arg1 = 8;
                        }
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Ring_Thread extends Thread {
        public void run()//??????run??????
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//???????????????
                outputStream1.write("O".getBytes());//????????????
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class StpRing_Thread extends Thread {
        public void run()//??????run??????
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//???????????????
                outputStream1.write("C".getBytes());//????????????
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Sleep_Thread extends Thread {
        public void run()//??????run??????
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//???????????????
                outputStream1.write("S".getBytes());//????????????
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class StpSleep_Thread extends Thread {
        public void run()//??????run??????
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//???????????????
                outputStream1.write("W".getBytes());//????????????
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void navigateTo(BDLocation location){
        if (isFirstLocate){
            LatLng lng = new LatLng(location.getLatitude(),location.getLongitude());//???????????????
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(lng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);//?????????????????????????????????3-19
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData = builder.build();
        baiduMap.setMyLocationData(locationData);
        x[Count] = location.getLatitude();
        y[Count] = location.getLongitude();
        Count++;
        if(Count>=3){
            Count = 0;
        }
    }
    private void requestLocation(){
        InitLocation();
        Client.start();
    }
    //???????????????
    private void InitLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//???????????????
        option.setScanSpan(5000);//ms
        option.setIsNeedAddress(true);//????????????????????????
        Client.setLocOption(option);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Client.stop();
        BaiduMap.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
    @Override
    protected void onResume() {
        super.onResume();
        BaiduMap.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        BaiduMap.onPause();
    }
    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(bdLocation);
            }
        }
    }

    private void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //????????????
    protected void sendSMSMessage() {
        Log.i("Send SMS", "");
        //????????????????????????????????????
        SharedPreferences sp2 = getSharedPreferences("user.xml",MODE_PRIVATE);
        String phoneNo = sp2.getString("number","");
        String message = sp2.getString("mssg","");
        Message msg = new Message();
        msg.what = 1;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            msg.arg1 = 6;
            handler.sendMessage(msg);
        } catch (Exception e) {
            msg.arg1 = 7;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }
}
