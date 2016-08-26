package com.example.fidu;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	/** 蓝牙设备列表 */
	private ArrayList<BluetoothDevice> bluetoothDeviceList;
	/** 蓝牙适配器，它用于控制开启或者禁用蓝牙功能 */
	private BluetoothAdapter bluetoothAdapter;
	/** 蓝牙设备列表适配器 */
	BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;
	/** 蓝牙通信的Sokect */
	private BluetoothSocket bluetoothSocket;
	/** 蓝牙通信的输出流 */
	private OutputStream outputStream;
	/** 开灯 */
	private static final int TYPE_OPEN = 0;
	/** 关灯 */
	private static final int TYPE_CLOSE = 1;
	/** 点动 */
	private static final int TYPE_DOT = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		findViewById(R.id.btn_open_bluetooth).setOnClickListener(this);
		findViewById(R.id.btn_close_bluetooth).setOnClickListener(this);
		findViewById(R.id.btn_discovery).setOnClickListener(this);
		findViewById(R.id.btn_stop_discovery).setOnClickListener(this);
		findViewById(R.id.btn_open).setOnClickListener(this);
		findViewById(R.id.btn_close).setOnClickListener(this);

		
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(mOnItemClickListener);
		bluetoothDeviceList = new ArrayList<BluetoothDevice>();
		mBluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(bluetoothDeviceList);
		listView.setAdapter(mBluetoothDeviceListAdapter);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_open_bluetooth:	// 打开蓝牙功能
			openBluetooth();
			break;
		case R.id.btn_close_bluetooth:	// 关闭蓝牙功能
			closeBluetooth();
			break;
		case R.id.btn_discovery:		// 扫描蓝牙设备
			startDiscovery();
			break;
		case R.id.btn_stop_discovery:	// 停止扫描蓝牙设备
			stopDiscovery();
			break;
		case R.id.btn_open:				// 开灯
			sendCtrlCommand(TYPE_OPEN);
			break;
		case R.id.btn_close:			// 关灯
			sendCtrlCommand(TYPE_CLOSE);
			break;

		}
	}
	
	/** 发送控制指令 */
	private void sendCtrlCommand(int type) {
		if (outputStream == null) {
			showToast("请先连接蓝牙设备");
			return;
		}
		
		byte[] commands = new byte[5];
		commands[0] = (byte) 0x01;
		commands[1] = (byte) 0x99;
		
		switch (type) {
		case TYPE_OPEN:
			// 开灯
			commands[2] = (byte) 0x01;
			commands[3] = (byte) 0x02;
			break;
		case TYPE_CLOSE:
			// 关灯
			commands[2] = (byte) 0x01;
			commands[3] = (byte) 0x01;
			break;


		default:
			break;
		}
		
		commands[4] = (byte) 0x99;
		try {
			outputStream.write(commands);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 停止扫描 */
	private void stopDiscovery() {
		if (!bluetoothAdapter.isEnabled()) {
			showToast("请先打开蓝牙功能");
			return;
		}
		
		bluetoothAdapter.cancelDiscovery();	// 停止扫描
	}

	/** 开始扫描蓝牙设备 */
	private void startDiscovery() {
		if (!bluetoothAdapter.isEnabled()) {
			showToast("请先打开蓝牙功能");
			return;
		}
		
		clearDeviceList();
		
		registerBluetoothDiscoveryReceiver();
		bluetoothAdapter.startDiscovery();	// 开始扫描蓝牙设备
	}

	/** 清除设备列表 */
	private void clearDeviceList() {
		if (!bluetoothDeviceList.isEmpty()) {
			bluetoothDeviceList.clear();
			mBluetoothDeviceListAdapter.notifyDataSetChanged();
		}
	}

	/** 注册蓝牙扫描结果接收者 */
	private void registerBluetoothDiscoveryReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);					// 扫描到蓝牙设备
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);	// 扫描开始
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);	// 扫描结束
		registerReceiver(bluetoothDiscoveryReceiver, filter);
	}
	
	BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// 扫描开始
				showToast("扫描开始");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// 扫描结束
				showToast("扫描结束");
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// 扫描到蓝牙设备
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				bluetoothDeviceList.add(device);
				mBluetoothDeviceListAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private void showToast(String string) {
		Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/** 禁用蓝牙功能 */
	private void closeBluetooth() {
		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
		}
		
		clearDeviceList();
	}

	/** 开启蓝牙功能 */
	private void openBluetooth() {
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}
	}
	
	OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BluetoothDevice device = bluetoothDeviceList.get(position);
			connectBluetoothDevice(device);
		}
	};

	protected void connectBluetoothDevice(final BluetoothDevice device) {
		new Thread() {

			@Override
			public void run() {
				try {
					bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
					bluetoothSocket.connect();	// 开始连接蓝牙设备
					outputStream = bluetoothSocket.getOutputStream();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							clearDeviceList();
							showToast("连接成功");
						}
					});
				} catch (IOException e) {
					release();
					e.printStackTrace();
				}
			};
		}.start();
	}

	/** 释放蓝牙相关资源 */
	protected void release() {
		if (bluetoothSocket != null) {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		release();
		super.onDestroy();
	}

}
