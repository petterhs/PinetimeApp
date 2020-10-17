package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.example.infinitimeapp.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BluetoothDevices {
    public static class BTDeviceModel {
        public String name;
        public String mac;

        public BTDeviceModel(String mac, String name) {
            this.name = name;
            this.mac = mac;
        }
    }

    private static BluetoothDevices instance = new BluetoothDevices();
    private final ArrayList<BTDeviceModel> deviceList;

    private BluetoothDevices() {
        deviceList = new ArrayList<>();
    }

    public static BluetoothDevices getInstance() {
        return instance;
    }

    public ArrayList<BTDeviceModel> getDevices(){
        return this.deviceList;
    }

    public void addDevice(BTDeviceModel device) {
        for(BTDeviceModel d: deviceList) {
            if(d.mac.equals(device.mac)) {
                return;
            }
        }

        deviceList.add(device);

        MainActivity.mAdapter.notifyDataSetChanged();
    }

    public BTDeviceModel getDeviceFromIndex(int index) {
        return deviceList.get(index);
    }

    public int getSize() {
        return this.deviceList.size();
    }

    public void clear() {
        deviceList.clear();
        MainActivity.mAdapter.notifyDataSetChanged();
    }
}
