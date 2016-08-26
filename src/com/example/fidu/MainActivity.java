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

	/** �����豸�б� */
	private ArrayList<BluetoothDevice> bluetoothDeviceList;
	/** �����������������ڿ��ƿ������߽����������� */
	private BluetoothAdapter bluetoothAdapter;
	/** �����豸�б������� */
	BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;
	/** ����ͨ�ŵ�Sokect */
	private BluetoothSocket bluetoothSocket;
	/** ����ͨ�ŵ������ */
	private OutputStream outputStream;
	/** ���� */
	private static final int TYPE_OPEN = 0;
	/** �ص� */
	private static final int TYPE_CLOSE = 1;
	/** �㶯 */
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
		case R.id.btn_open_bluetooth:	// ����������
			openBluetooth();
			break;
		case R.id.btn_close_bluetooth:	// �ر���������
			closeBluetooth();
			break;
		case R.id.btn_discovery:		// ɨ�������豸
			startDiscovery();
			break;
		case R.id.btn_stop_discovery:	// ֹͣɨ�������豸
			stopDiscovery();
			break;
		case R.id.btn_open:				// ����
			sendCtrlCommand(TYPE_OPEN);
			break;
		case R.id.btn_close:			// �ص�
			sendCtrlCommand(TYPE_CLOSE);
			break;

		}
	}
	
	/** ���Ϳ���ָ�� */
	private void sendCtrlCommand(int type) {
		if (outputStream == null) {
			showToast("�������������豸");
			return;
		}
		
		byte[] commands = new byte[5];
		commands[0] = (byte) 0x01;
		commands[1] = (byte) 0x99;
		
		switch (type) {
		case TYPE_OPEN:
			// ����
			commands[2] = (byte) 0x01;
			commands[3] = (byte) 0x02;
			break;
		case TYPE_CLOSE:
			// �ص�
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

	/** ֹͣɨ�� */
	private void stopDiscovery() {
		if (!bluetoothAdapter.isEnabled()) {
			showToast("���ȴ���������");
			return;
		}
		
		bluetoothAdapter.cancelDiscovery();	// ֹͣɨ��
	}

	/** ��ʼɨ�������豸 */
	private void startDiscovery() {
		if (!bluetoothAdapter.isEnabled()) {
			showToast("���ȴ���������");
			return;
		}
		
		clearDeviceList();
		
		registerBluetoothDiscoveryReceiver();
		bluetoothAdapter.startDiscovery();	// ��ʼɨ�������豸
	}

	/** ����豸�б� */
	private void clearDeviceList() {
		if (!bluetoothDeviceList.isEmpty()) {
			bluetoothDeviceList.clear();
			mBluetoothDeviceListAdapter.notifyDataSetChanged();
		}
	}

	/** ע������ɨ���������� */
	private void registerBluetoothDiscoveryReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);					// ɨ�赽�����豸
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);	// ɨ�迪ʼ
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);	// ɨ�����
		registerReceiver(bluetoothDiscoveryReceiver, filter);
	}
	
	BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// ɨ�迪ʼ
				showToast("ɨ�迪ʼ");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// ɨ�����
				showToast("ɨ�����");
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// ɨ�赽�����豸
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

	/** ������������ */
	private void closeBluetooth() {
		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.disable();
		}
		
		clearDeviceList();
	}

	/** ������������ */
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
					bluetoothSocket.connect();	// ��ʼ���������豸
					outputStream = bluetoothSocket.getOutputStream();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							clearDeviceList();
							showToast("���ӳɹ�");
						}
					});
				} catch (IOException e) {
					release();
					e.printStackTrace();
				}
			};
		}.start();
	}

	/** �ͷ����������Դ */
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
