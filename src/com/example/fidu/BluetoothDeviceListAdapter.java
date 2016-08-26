package com.example.fidu;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 蓝牙设备列表的适配器
 * @author Administrator
 *
 */
public class BluetoothDeviceListAdapter extends BaseAdapter {

	ArrayList<BluetoothDevice> bluetoothDeviceList;
	
	public BluetoothDeviceListAdapter(ArrayList<BluetoothDevice> bluetoothDeviceList) {
		this.bluetoothDeviceList = bluetoothDeviceList;
	}
	
	@Override
	public int getCount() {
		return bluetoothDeviceList == null ? 0 : bluetoothDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return bluetoothDeviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.adapter_bluetooth_list, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BluetoothDevice device = bluetoothDeviceList.get(position);
		
		holder.tv_name.setText(device.getName());
		holder.tv_address.setText(device.getAddress());
		
		return convertView;
	}
	
	class ViewHolder {
		/** 蓝牙名称 */
		TextView tv_name;
		/** 蓝牙地址 */
		TextView tv_address;
	}

}
