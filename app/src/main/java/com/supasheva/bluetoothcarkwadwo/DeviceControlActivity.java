package com.supasheva.bluetoothcarkwadwo;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeviceControlActivity extends Activity {
  private final static String TAG = DeviceControlActivity.class.getSimpleName();
//
//  public static final String DEVICE1_ON = "f";
//  public static final String DEVICE1_OFF = "g";
//  public static final String DEVICE2_ON = "h";
//  public static final String DEVICE2_OFF = "j";


  public static final String MOVE_FORWARD="f";
  public static final String TURN_LEFT="l";
  public static final String TURN_RIGHT="r";
  public static final String MOVE_BACK = "b";



  public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
  public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
  private int[] RGBFrame = {0, 0, 0};
  private TextView isSerial;
  private TextView mConnectionState;

  private Button forwardBtn,backBtn,leftBtn,rightBtn;

  private ToggleButton toggle1, toggle2;
  private String mDeviceName;
  private String mDeviceAddress;
  //  private ExpandableListView mGattServicesList;
  private BluetoothLeService mBluetoothLeService;
  private boolean mConnected = false;
  private BluetoothGattCharacteristic characteristicTX;
  private BluetoothGattCharacteristic characteristicRX;


  public final static UUID HM_RX_TX =
    UUID.fromString(SampleGattAttributes.HM_RX_TX);

  private final String LIST_NAME = "NAME";
  private final String LIST_UUID = "UUID";

  // Code to manage Service lifecycle.
  private final ServiceConnection mServiceConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
      mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
      if (!mBluetoothLeService.initialize()) {
        Log.e(TAG, "Unable to initialize Bluetooth");
        finish();
      }
      // Automatically connects to the device upon successful start-up initialization.
      mBluetoothLeService.connect(mDeviceAddress);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      mBluetoothLeService = null;
    }
  };

  // Handles various events fired by the Service.
  // ACTION_GATT_CONNECTED: connected to a GATT server.
  // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
  // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
  // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
  //                        or notification operations.
  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
        mConnected = true;
        updateConnectionState(R.string.connected);
        invalidateOptionsMenu();
      } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
        mConnected = false;
        updateConnectionState(R.string.disconnected);
        invalidateOptionsMenu();
      } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
        // Show all the supported services and characteristics on the user interface.
        displayGattServices(mBluetoothLeService.getSupportedGattServices());
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.device_control_activity);

    ActionBar bar = getActionBar();
    bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

    final Intent intent = getIntent();
    mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
    mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

    // Sets up UI references.
    ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
    mConnectionState = (TextView) findViewById(R.id.connection_state);
    // is serial present?
    isSerial = (TextView) findViewById(R.id.isSerial);

    getActionBar().setTitle(mDeviceName);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

//    toggle1 = (ToggleButton) findViewById(R.id.toggleButton1);
//    toggle1.setText(null);
//    toggle1.setTextOn(null);
//    toggle1.setTextOff(null);
//    toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
//    toggle2.setText(null);
//    toggle2.setTextOn(null);
//    toggle2.setTextOff(null);

    forwardBtn =  findViewById(R.id.forwardBtn);
      backBtn = findViewById(R.id.backBtn);
      leftBtn = findViewById(R.id.leftBtn);
        rightBtn=findViewById(R.id.rightBtn);

//    toggle1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (isChecked) {
//          sendDataToBLE(DEVICE1_ON);
//          toggle1.setBackgroundResource(R.drawable.light1);
//        } else {
//          sendDataToBLE(DEVICE1_OFF);
//          toggle1.setBackgroundResource(R.drawable.light0);
//        }
//      }
//    });

    forwardBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendDataToBLE(MOVE_FORWARD);
      }
    });

    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendDataToBLE(MOVE_BACK);
      }
    });

    leftBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendDataToBLE(TURN_LEFT);
      }
    });

    rightBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendDataToBLE(TURN_RIGHT);
      }
    });


  }

  void sendDataToBLE(String str) {
    Log.d(TAG, "Sending result=" + str);
    final byte[] tx = str.getBytes();
    if (mConnected) {
      characteristicTX.setValue(tx);
      mBluetoothLeService.writeCharacteristic(characteristicTX);
      mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    if (mBluetoothLeService != null) {
      final boolean result = mBluetoothLeService.connect(mDeviceAddress);
      Log.d(TAG, "Connect request result=" + result);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver(mGattUpdateReceiver);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(mServiceConnection);
    mBluetoothLeService = null;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.gatt_services, menu);
    if (mConnected) {
      menu.findItem(R.id.menu_connect).setVisible(false);
      menu.findItem(R.id.menu_disconnect).setVisible(true);
    } else {
      menu.findItem(R.id.menu_connect).setVisible(true);
      menu.findItem(R.id.menu_disconnect).setVisible(false);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_connect:
        mBluetoothLeService.connect(mDeviceAddress);
        return true;
      case R.id.menu_disconnect:
        mBluetoothLeService.disconnect();
        return true;
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateConnectionState(final int resourceId) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mConnectionState.setText(resourceId);
      }
    });
  }

  // Demonstrates how to iterate through the supported GATT Services/Characteristics.
  // In this sample, we populate the data structure that is bound to the ExpandableListView
  // on the UI.
  private void displayGattServices(List<BluetoothGattService> gattServices) {
    if (gattServices == null) return;
    String uuid = null;
    String unknownServiceString = getResources().getString(R.string.unknown_service);
    ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


    // Loops through available GATT Services.
    for (BluetoothGattService gattService : gattServices) {
      HashMap<String, String> currentServiceData = new HashMap<String, String>();
      uuid = gattService.getUuid().toString();
      currentServiceData.put(
        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

      // If the service exists for HM 10 Serial, say so.
      if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
        isSerial.setText("Yes");
      } else {
        isSerial.setText("No");
      }
      currentServiceData.put(LIST_UUID, uuid);
      gattServiceData.add(currentServiceData);

      // get characteristic when UUID matches RX/TX UUID
      characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
      characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
    }

  }

  private static IntentFilter makeGattUpdateIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
    intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
    return intentFilter;
  }
}
