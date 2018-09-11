package com.example.fubuki.outdoor_navigation;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,View.OnClickListener{

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;

    private LocationClient locationClient;

    public MyLocationListenner myListener = new MyLocationListenner();

    boolean isFirstLoc = true; // 是否首次定位

    private SensorManager sm;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;
    private float orientationX;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] orientationValues = new float[3];

    private final String TAG = "Activity";

    private List<Object> locationTuple = new ArrayList<Object>(); //保存定位触发的三元组

    private double globalLatitude,globalLongitude;

    private GpsNode gpsPointSet;

    //蓝牙
    private List<String> bluetoothDevices = new ArrayList<String>(); //保存搜索到的列表
    private ArrayAdapter<String> arrayAdapter; //ListView的适配器

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private BluetoothGatt bluetoothGatt;
    //bluetoothDevice是dervices中选中的一项 bluetoothDevice=dervices.get(i);
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothDevice bluetoothDevice;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();//存放扫描结果

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private static final int UPDATE_STATUS= 1;
    private static final int DISCONN_BLE = 2;
    private static final int UPDATE_LIST = 3;
    private static final int TIMER_LOCATION = 4;
    private static final int NEW_DISTANCE = 5;

    private double rcvDis; //从终端接收回来的距离

    private int positionNumber;

    //定时器相关
    private final Timer locationTimer = new Timer();
    private TimerTask locationTask;
    private double currentLatitude,currrentLongitude,lastLatitude,lastLongitude;

    //线程相关测试
    private class Token {
        private boolean flag;
        public Token() {
            setFlag(false);
        }
        public void setFlag(boolean flag) {
            this.flag = flag;
        }
        public boolean getFlag() {
            return flag;
        }
    }
    private Token token = null;

    private boolean toggleFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchBtn = findViewById(R.id.getLocation);

        searchBtn.setOnClickListener(this);

        Button setBtn = findViewById(R.id.setBtn);

        setBtn.setOnClickListener(this);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        locationClient = new LocationClient(this);
        //注册监听
        locationClient.registerLocationListener(myListener);
        //定位配置信息
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//定位请求时间间隔
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        locationClient.setLocOption(option);
        //开启定位
        locationClient.start();

        //传感器相关
        orientationX = 0;
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sm.registerListener(mySensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(mySensorListener, mSensor,SensorManager.SENSOR_DELAY_NORMAL);

        //更新显示数据的方法
        calculateOrientation();

        //蓝牙
        // 检查手机是否支持BLE，不支持则退出
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持蓝牙BLE，将关闭", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);  // 弹对话框的形式提示用户开启蓝牙
        }

        Button searchBLEBtn = findViewById(R.id.searchBtn);
        searchBLEBtn.setOnClickListener(this);

        gpsPointSet = new GpsNode();

        positionNumber = 0;

        rcvDis = 0;

        //定时相关
        currentLatitude = 0;
        currrentLongitude = 0;
        lastLatitude = 0;
        lastLongitude = 0;
        locationTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message message = new Message();
                message.what = TIMER_LOCATION;
                handler.sendMessage(message);
            }
        };
        locationTimer.schedule(locationTask, 1000, 2000);

        token = new Token();
        if(!token.getFlag())
            Log.e("A","the token flag value is null");
        else
            Log.e("A","the token flag value is"+token.getFlag());
    }

    final SensorEventListener mySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = sensorEvent.values;
            calculateOrientation();
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, orientationValues);
        if(orientationValues[0] < 0)
            orientationValues[0] = orientationValues[0] + (float)(2*Math.PI);
        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(orientationValues[0]);
        if(Math.abs(values[0] - orientationX) > 3.0){
            orientationX = values[0];
            //Log.e(TAG,"text:"+values[0]);
        }
    }

    //断开BLE连接
    private void disconnect_BLE(){
        bluetoothGatt.disconnect();
        Message tempMsg = new Message();
        tempMsg.what = DISCONN_BLE;
        handler.sendMessage(tempMsg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        locationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);

    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.getLocation:
                //缺distance
                Log.e(TAG,"相关信息:"+globalLatitude+" "+globalLongitude+" "+orientationValues[0]+" "+rcvDis);
                GpsPoint currentGpsPoint = new GpsPoint(globalLongitude,globalLatitude,orientationValues[0],rcvDis);
                gpsPointSet.addGpsPoint(currentGpsPoint);
                if(gpsPointSet.getNodeNumber() > 1){
                    Point nodePosition = gpsPointSet.getNodePosition();
                    Log.e(TAG,"x:"+nodePosition.getX()+"   "+"y:"+nodePosition.getY());
                    //TODO:要显示在屏幕上
                    mBaiduMap.clear();
                    LatLng point = new LatLng(nodePosition.getY(), nodePosition.getX());

                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marka);

                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);

                    mBaiduMap.addOverlay(option);

                    LatLng llText = new LatLng(nodePosition.getY(), nodePosition.getX());

//构建文字Option对象，用于在地图上添加文字
                    OverlayOptions textOption = new TextOptions()
                            .bgColor(0xAAFFFF00)
                            .fontSize(24)
                            .fontColor(0xFFFF00FF)
                            .text(Integer.toString(positionNumber++))
                            .position(llText);

//在地图上添加该文字对象并显示
                    mBaiduMap.addOverlay(textOption);
                }else{}
                break;
            case R.id.searchBtn: //蓝牙
                if(bluetoothGatt == null){
                    actionAlertDialog();
                }else{
                    disconnect_BLE();
                    bluetoothGatt = null;
                }
                break;
            case R.id.setBtn:
                EditText msg = findViewById(R.id.editText);
                String[] strArray = null;
                strArray = msg.getText().toString().split(",");
                //Log.e(TAG,strArray[0]);
                gpsPointSet.setAccuracy(convertToDouble(strArray[0],0)/100000,convertToDouble(strArray[1],0)/100000);
                //gpsPointSet.getAccuracy();
                break;
            default:
                break;
        }
    }

    private void actionAlertDialog(){
        View bottomView = View.inflate(MainActivity.this,R.layout.ble_devices,null);//填充ListView布局
        ListView lvDevices = (ListView) bottomView.findViewById(R.id.device_list);//初始化ListView控件
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                bluetoothDevices);
        lvDevices.setAdapter(arrayAdapter);
        lvDevices.setOnItemClickListener(this);

        builder= new AlertDialog.Builder(MainActivity.this)
                .setTitle("蓝牙列表").setView(bottomView);//在这里把写好的这个listview的布局加载dialog中
        alertDialog = builder.create();
        alertDialog.show();

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(scanCallback);//android5.0把扫描方法单独弄成一个对象了（alt+enter添加），扫描结果储存在devices数组中。最好在startScan()前调用stopScan()。

        handler.postDelayed(runnable, 10000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothDevice = devices.get(position);
        bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    alertDialog.dismiss();

                    Message tempMsg = new Message();
                    tempMsg.what = UPDATE_STATUS;
                    handler.sendMessage(tempMsg);

                    try {
                        Thread.sleep(600);
                        Log.i(TAG, "Attempting to start service discovery:"
                                + gatt.discoverServices());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        Log.i(TAG, "Fail to start service discovery:");
                        e.printStackTrace();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //setTitle("连接断开");
                }
                return;
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                //此函数用于接收数据
                super.onServicesDiscovered(gatt, status);
                Log.d(TAG, "Hi discovered!");
                String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
                String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

                bluetoothGattService = bluetoothGatt.getService(UUID.fromString(service_UUID));
                bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));

                if (bluetoothGattCharacteristic != null) {
                    gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true); //用于接收数据
                    //Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_LONG).show();
                    for (BluetoothGattDescriptor dp : bluetoothGattCharacteristic.getDescriptors()) {
                        if (dp != null) {
                            if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else if ((bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            gatt.writeDescriptor(dp);
                        }
                    }
                    Log.d(TAG, "服务连接成功");
                } else {
                    //Toast.makeText(MainActivity.this, "发现服务失败", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "服务失败");
                    return;
                }
                return;
            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                super.onCharacteristicChanged(gatt,characteristic);
                //发现服务后的响应函数
                byte[] bytesReceive = characteristic.getValue();
                String msgStr = new String(bytesReceive);
                //Pattern pattern = Pattern.compile("([0-9]+.[0-9]+)");
                Pattern pattern = Pattern.compile("[(?<=addr|dis) (?=end)]+");
                String[] strs = pattern.split(msgStr);

                rcvDis = convertToDouble(strs[1],0);
                Log.e(TAG,"接收到的距离："+rcvDis);

                Message tempMsg = new Message();
                tempMsg.what = NEW_DISTANCE;
                handler.sendMessage(tempMsg);
                toggleFlag = !toggleFlag;

                if(token.getFlag()) {
                    synchronized (token) {
                        token.setFlag(false);
                        token.notifyAll();
                        Log.e(TAG,"线程重新启动");
                    }
                }
                return;
            }
        });
    }
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult results) {
            super.onScanResult(callbackType, results);
            BluetoothDevice device = results.getDevice();
            if (!devices.contains(device)) {  //判断是否已经添加
                devices.add(device);//也可以添加devices.getName()到列表，这里省略            }
                // callbackType：回调类型
                // result：扫描的结果，不包括传统蓝牙        }
                bluetoothDevices.add(device.getName() + ":"
                        + device.getAddress() + "\n");
                //更新字符串数组适配器，显示到listview中
                Message tempMsg = new Message();
                tempMsg.what = UPDATE_LIST;
                handler.sendMessage(tempMsg);
            }
        }
    };
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(orientationX).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, BitmapDescriptorFactory.fromResource(R.drawable.arrow));
            mBaiduMap.setMyLocationConfiguration(config);

            //获取并显示
            globalLatitude = location.getLatitude();
            globalLongitude = location.getLongitude();

            //定时器相关
            currentLatitude = globalLatitude;
            currrentLongitude = globalLongitude;

            LatLng llText = new LatLng(globalLatitude, globalLongitude);

//构建文字Option对象，用于在地图上添加文字
            OverlayOptions textOption = new TextOptions()
                    .bgColor(0xAAFFFF00)
                    .fontSize(24)
                    .fontColor(0xFFFF00FF)
                    .text("位置消息：纬度："+globalLatitude+" 经度："+globalLongitude)
                    .position(llText);

//在地图上添加该文字对象并显示
           // mBaiduMap.addOverlay(textOption);

           /* LatLng point = new LatLng(globalLatitude, globalLongitude);

//构建Marker图标

            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_marka);

//构建MarkerOption，用于在地图上添加Marker

            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);

//在地图上添加Marker，并显示

            mBaiduMap.addOverlay(option);*/
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private void getCurrentLocation(){
        LatLng p1 = new LatLng(currentLatitude,currrentLongitude);
        LatLng p2 = new LatLng(lastLatitude,lastLongitude);

        double distance = DistanceUtil.getDistance(p1,p2);

        /*if(distance > 10){
            //TODO
            //此处是距离大于十米的操作
          if(bluetoothGattCharacteristic != null) {
              synchronized (token) {
                  try {
                      token.setFlag(true);
                      Log.e(TAG, "线程挂起");
                      token.wait();
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
        }*/
        lastLongitude = currrentLongitude;
        lastLatitude = currentLatitude;
    }
    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            Button searchBtn = findViewById(R.id.searchBtn);
            switch (msg.what){
                case UPDATE_STATUS:
                    searchBtn.setText("断开蓝牙");
                    searchBtn.setBackgroundResource(R.drawable.cancelbutton);
                    break;
                case DISCONN_BLE:
                    searchBtn.setText("搜索蓝牙");
                    searchBtn.setBackgroundResource(R.drawable.buttonshape);
                    break;
                case UPDATE_LIST:
                    arrayAdapter.notifyDataSetChanged();
                    break;
                case TIMER_LOCATION:
                    getCurrentLocation();
                    break;
                case NEW_DISTANCE:
                    TextView distanceText = findViewById(R.id.distance);
                    distanceText.setText("位置信息："+rcvDis+" "+ toggleFlag);
                    break;
                default:
                    break;
            }
        }
    };//import android.os.Handler;

    //string转float
    public static double convertToDouble(String number, float defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            return defaultValue;
        }

    }

}
