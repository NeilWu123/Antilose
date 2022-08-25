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
    private com.baidu.mapapi.map.BaiduMap baiduMap;//地图总控制器
    private boolean isFirstLocate = true;
    boolean isRing = false;
    boolean isSleep = false;
    boolean isLoct = false;
    boolean isDist = false;
    int count = 0;
    int Count = 0;
    int RSSI_Value = 0;
    double Distance = 0;
    Socket socket = null;//定义socket
    Button ConnectBtn;//连接按钮
    Button RingBtn;//主动报警按钮
    Button sendBtn;//发送短信按钮
    Button SleepBtn;//监听模式
    Button DistBtn;//获取距离
    Button LoctBtn;//定位
    String IPEditText;//定义ip输入框
    EditText PortText;//定义端口输入框
    private TextView DistTV;//显示距离
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
                        Toast.makeText(getApplicationContext(), "声光报警中",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "已停止报警",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(getApplicationContext(), "连接失败！",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(getApplicationContext(), "进入省电监听模式",
                                Toast.LENGTH_SHORT).show();
                        SleepBtn.setText("唤醒");
                        break;
                    case 5:
                        Toast.makeText(getApplicationContext(), "已恢复正常工作模式",
                                Toast.LENGTH_SHORT).show();
                        SleepBtn.setText("省电模式");
                        break;
                    case 6:
                        Toast.makeText(getApplicationContext(), "报警短信已发送",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(getApplicationContext(), "短信发送失败，请再次尝试！",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(getApplicationContext(), "连接成功",
                                Toast.LENGTH_SHORT).show();
                        ConnectBtn.setText("已连接");
                        ConnectBtn.setEnabled(false);
                        break;
                    default:
                        break;
                }
            }
            else if (msg.what == 2) {
                Distance = Math.pow(10,-0.0396*msg.arg1-2.2444);
                if (Distance < 30) {
                    DistTV.setText( "当前距离为"+String.format("%.2f", Distance)+"米");
                } else {
                    DistTV.setText( "Wi-Fi信号强度较差，无法获得有效距离");
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
//构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
//在地图上添加Marker，并显示
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

    //添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }
    //编辑菜单栏
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
                AlertDialog dialog = new AlertDialog.Builder(this).create();//创建对话框
                dialog.setTitle("Q&A");//设置对话框标题
                dialog.setMessage("①功能无法使用：请检查是否给予应用相关权限。\n②连接失败：请查看编号是否输入正确。\n③无法发送短信：请先编辑。");//设置文字显示内容
                //分别设置2个button
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//关闭对话框
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//关闭对话框
                    }
                });
                dialog.show();//显示对话框
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //一定要在setContentView（）前调用
        SDKInitializer.initialize(getApplicationContext());
        Client = new LocationClient(getApplicationContext());//获取全局Context
        Client.registerLocationListener((BDLocationListener) new MyLocationListener());//注册一个定位监听器，获取位置信息，回调此定位监听器
        setContentView(R.layout.activity_main);
        BaiduMap = (MapView) findViewById(R.id.baiduMap);
        baiduMap = BaiduMap.getMap();//获取实例，可以对地图进行一系列操作，比如：缩放范围，移动地图
        baiduMap.setMyLocationEnabled(true);//允许当前设备显示在地图上
        requestLocation();

        ConnectBtn = (Button) findViewById(R.id.Connect_Bt);//连接
        RingBtn = (Button) findViewById(R.id.Ring_Bt);//主动报警
        SleepBtn = (Button) findViewById(R.id.Sleep_Bt);//监听模式
        DistBtn = (Button) findViewById(R.id.Dist_Bt);//获取距离
        sendBtn = (Button) findViewById(R.id.btnSendSMS);//报警短信
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
        PortText = (EditText) findViewById(R.id.Port_ET);//获得端口文本框按钮对象
        DistTV = (TextView) findViewById(R.id.Dist_TV);//显示距离文本框
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
                        RingBtn.setText("无连接");
                    } else {
                        try {
                            if (!isRing) {
                                RingBtn.setText("停止报警");
                                Ring_Thread Ring_Thread1 = new Ring_Thread();
                                Ring_Thread1.start();
                                isRing = true;
                            } else {
                                RingBtn.setText("声光报警");
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
                        SleepBtn.setText("无连接");
                    } else {
                        try {
                            if (!isSleep) {
                                //SleepBtn.setText("唤醒");
                                Sleep_Thread Sleep_Thread1 = new Sleep_Thread();
                                Sleep_Thread1.start();
                                isSleep = true;
                            } else {
                                //SleepBtn.setText("省电模式");
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
                        DistBtn.setText("无连接");
                    } else {
                        timer.scheduleAtFixedRate(task, 200, 500);
                        DistBtn.setEnabled(false);
                        isDist = true;
                    }
                    break;
                case R.id.Loct_Bt:
                    if (socket == null) {
                        LoctBtn.setText("无连接");
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
                   InetAddress ipAddress = InetAddress.getByName(IPEditText);//用InetAddress方法获取ip地址
                   int port = Integer.valueOf(PortText.getText().toString()) + 8000;//获取端口号
                   socket = new Socket(ipAddress, port);//创建连接地址和端口
                   Receive_Thread Receive_Thread1 = new Receive_Thread();//在创建完连接后启动接收线程
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
                    final byte[] buf = new byte[1024];//创建接收缓冲区
                    inputStream = socket.getInputStream();
                    final int len = inputStream.read(buf);//可通过循环一次接收多指令
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
        public void run()//重写run方法
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//获取输出流
                outputStream1.write("O".getBytes());//发送数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class StpRing_Thread extends Thread {
        public void run()//重写run方法
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//获取输出流
                outputStream1.write("C".getBytes());//发送数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Sleep_Thread extends Thread {
        public void run()//重写run方法
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//获取输出流
                outputStream1.write("S".getBytes());//发送数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class StpSleep_Thread extends Thread {
        public void run()//重写run方法
        {
            try {
                OutputStream outputStream1 = socket.getOutputStream();//获取输出流
                outputStream1.write("W".getBytes());//发送数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void navigateTo(BDLocation location){
        if (isFirstLocate){
            LatLng lng = new LatLng(location.getLatitude(),location.getLongitude());//指定经纬度
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(lng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);//百度地图缩放级别限定在3-19
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
    //初始化地图
    private void InitLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精确模式
        option.setScanSpan(5000);//ms
        option.setIsNeedAddress(true);//获取详细信息许可
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

    //发送短信
    protected void sendSMSMessage() {
        Log.i("Send SMS", "");
        //建立本地文件储存报警信息
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
