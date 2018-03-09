package com.example.rjyx.myapplication;

public class DeviceBean {
	protected boolean isReceive;
	protected String name;
	protected String Address;


	public DeviceBean(String name,String address, boolean isReceive) {
		this.name = name;
		this.Address = address;
		this.isReceive = isReceive;
	}
}