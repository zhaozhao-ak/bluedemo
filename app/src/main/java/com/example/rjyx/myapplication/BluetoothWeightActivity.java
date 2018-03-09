package com.example.rjyx.myapplication;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by RJYX on 2018/3/6.
 */

public class BluetoothWeightActivity  extends AppCompatActivity implements BluetoothItemOnclick {

    private String TAG = "BluetoothWeightActivity";
    private ListView mListView;
    private TextView tv_msg;
    private Button mBtnSend;// 发送按钮
    private Button mBtnDisconn;// 断开连接
    private EditText mEtMsg;
    private TextView tv_service_state,tv_connect_state,tv_communication_state;
    //数据
    private ArrayList<DeviceBean> mDatas;
    private Button mBtnSearch, mBtnService;
    private ChatListAdapter mAdapter;
    //蓝牙适配器
    private BluetoothAdapter mBtAdapter;

    private String BlueToothAddress;//蓝牙地址
    private static final int STATUS_CONNECT = 0x11;

    /* 一些常量，代表服务器的名称 */
    public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
    public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
    public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";

    // 蓝牙服务端socket
    private BluetoothServerSocket mServerSocket;
    // 蓝牙客户端socket
    private BluetoothSocket mSocket;
    // 设备
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;

    // --线程类-----------------
    private ServerThread mServerThread;
    private ClientThread mClientThread;
    private ReadThread mReadThread;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices);
        initDatas();
        initViews();
        registerBroadcast();
        init();
        initEvents();
    }

    @Override
    public void onResume() {
        super.onResume();

        //开启蓝牙服务端
        mServerThread = new ServerThread();
        mServerThread.start();
    }

    /**
     * 信息处理
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String info = (String) msg.obj;
            switch (msg.what) {
                //蓝牙服务器启动状态
                case 1:
                    tv_service_state.setText("已开启");
                    break;
                //蓝牙正在连接
                case 2:
                    tv_connect_state.setText("正在连接..");
                    break;
                //蓝牙正常连接显示蓝牙地址
                case 3:
                    tv_connect_state.setText(BlueToothAddress);
                    tv_communication_state.setText("正常");
                    break;
                    //接收到数据
                case 8:
                    tv_msg.setText(info);
                    break;

                //蓝牙连接异常
                case 10:
                    tv_connect_state.setText("连接异常..");

                    break;
                default:
                    break;
            }
        }

    };

    private void initDatas() {
        mDatas = new ArrayList<DeviceBean>();
        mAdapter = new ChatListAdapter(this, mDatas,this);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 列出所有的蓝牙设备
     */
    private void init() {
        Log.i("tag", "mBtAdapter=="+ mBtAdapter);
        //根据适配器得到所有的设备信息

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            mBtnSearch.setText("重新搜索");
        }
        mDatas.clear();
        mAdapter.notifyDataSetChanged();

        Set<BluetoothDevice> deviceSet = mBtAdapter.getBondedDevices();
        if (deviceSet.size() > 0) {
            for (BluetoothDevice device : deviceSet) {
                mDatas.add(new DeviceBean(device.getName(),device.getAddress(), true));
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mDatas.size() - 1);
            }
        }
        /* 开始搜索 */
        mBtAdapter.startDiscovery();

    }

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        //设备被发现广播
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, discoveryFilter);

        // 设备发现完成
        IntentFilter foundFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, foundFilter);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mListView = (ListView) findViewById(R.id.list_device);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        mEtMsg = (EditText) findViewById(R.id.MessageText);
        mEtMsg.clearFocus();
        mBtnSend = (Button) findViewById(R.id.btn_msg_send);
        mBtnDisconn = (Button) findViewById(R.id.btn_disconnect);
        tv_service_state = (TextView) findViewById(R.id.tv_service_state);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
        tv_communication_state = (TextView) findViewById(R.id.tv_communication_state);

        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);

        mBtnSearch = (Button) findViewById(R.id.start_seach);
        mBtnSearch.setOnClickListener(mSearchListener);


    }










    private void initEvents() {
        // 发送信息
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = mEtMsg.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    // 发送信息
                    sendMessageHandle(text);

                    mEtMsg.setText("");
                    mEtMsg.clearFocus();
                    // 隐藏软键盘
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(mEtMsg.getWindowToken(), 0);
                } else
                    Toast.makeText(BluetoothWeightActivity.this, "发送内容不能为空！", LENGTH_SHORT).show();
            }
        });

        // 关闭会话
        mBtnDisconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //取消配对的
                removePairDevice();

                //断开客户端连接
                shutdownClient();

                //断开服务端连接
//                shutdownServer();

                //重新扫描
                init();
                tv_connect_state.setText("");
                tv_communication_state.setText("不正常");
                Toast.makeText(BluetoothWeightActivity.this, "已断开连接！", LENGTH_SHORT).show();
            }
        });
    }

    // 发送数据
    private void sendMessageHandle(String msg) {
        if (mSocket == null) {
            Toast.makeText(this, "没有连接", LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = mSocket.getOutputStream();
            os.write(msg.getBytes());

//			mDatas.add(new DeviceBean(msg, false));
//			mAdapter.notifyDataSetChanged();
//			mListView.setSelection(mDatas.size() - 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 读取数据
    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = mSocket.getInputStream();
                while (true) {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = 8;
                        mHandler.sendMessage(msg);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }


    /* ͣ停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            public void run() {
                if (mClientThread != null) {
                    mClientThread.interrupt();
                    mClientThread = null;
                }
                if (mReadThread != null) {
                    mReadThread.interrupt();
                    mReadThread = null;
                }
                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mSocket = null;
                }
            };
        }.start();
    }

    /* 停止服务器 */
    private void shutdownServer() {
        new Thread() {
            public void run() {
                if (mServerThread != null) {
                    mServerThread.interrupt();
                    mServerThread = null;
                }
                if (mReadThread != null) {
                    mReadThread.interrupt();
                    mReadThread = null;
                }
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                    if (mServerSocket != null) {
                        mServerSocket.close();
                        mServerSocket = null;
                    }
                } catch (IOException e) {
                    Log.e("server", "mserverSocket.close()", e);
                }
            };
        }.start();
    }

    // 开启服务器
    private class ServerThread extends Thread {
        public void run() {
            try {
                // 创建一个蓝牙服务器 参数分别：服务器名称、UUID
                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                Message msg = new Message();
                msg.obj = "蓝牙服务开启，正在等待客户端的连接...";
                msg.what = 1;
                mHandler.sendMessage(msg);

				/* 接受客户端的连接请求 */
                mSocket = mServerSocket.accept();
                msg = new Message();
                msg.obj = "客户端已经连接上！可以发送信息。";
                msg.what = 3;
                mHandler.sendMessage(msg);
                // 启动接受数据
                mReadThread = new ReadThread();
                mReadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    // 客户端线程
    private class ClientThread extends Thread {
        public void run() {
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                Message msg = new Message();
                msg.obj = "请稍候，正在连接服务器:" + BlueToothAddress;
                msg.what = 2;
                mHandler.sendMessage(msg);

                mSocket.connect();

                msg = new Message();
                msg.obj = "已经连接上服务端！可以发送信息。";
                msg.what = 3;
                mHandler.sendMessage(msg);
                // 启动接受数据
                mReadThread = new ReadThread();
                mReadThread.start();
            } catch (IOException e) {
                Message msg = new Message();
                msg.obj = "连接服务端异常！断开连接重新试一试。";
                msg.what = 10;
                mHandler.sendMessage(msg);
            }
        }
    };

    /**
     * 搜索监听
     */
    private View.OnClickListener mSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
                mBtnSearch.setText("重新搜索");
            }
            mDatas.clear();
            mAdapter.notifyDataSetChanged();

            Set<BluetoothDevice> deviceSet = mBtAdapter.getBondedDevices();
            if (deviceSet.size() > 0) {
                for (BluetoothDevice device : deviceSet) {
                    mDatas.add(new DeviceBean(device.getName(),device.getAddress(), true));
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mDatas.size() - 1);
                }
            }

            /* 开始搜索 */
            mBtAdapter.startDiscovery();

        }
    };

    /**
     * 发现设备广播
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 获得设备信息
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果绑定的状态不一样
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mDatas.add(new DeviceBean(device.getName() ,device.getAddress(), false));
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mDatas.size() - 1);
                }
                // 如果搜索完成了
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                if (mListView.getCount() == 0) {
                    mDatas.add(new DeviceBean("没有配对的设备","null", false));
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mDatas.size() - 1);
                }
                mBtnSearch.setText("重新搜索");
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        //打开蓝牙
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }
    }

    @Override
    public void OnclickConnection(DeviceBean item) {

        final DeviceBean bean = item;
        BlueToothAddress = bean.Address;
        AlertDialog.Builder stopDialog = new AlertDialog.Builder(BluetoothWeightActivity.this);
        stopDialog.setTitle("连接");//标题
        stopDialog.setMessage(bean.Address);
        stopDialog.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mBtAdapter.cancelDiscovery();
                mBtnSearch.setText("重新搜索");

                //开启蓝牙客户端
                if (!"".equals(bean.Address)) {
                    mDevice = mBluetoothAdapter.getRemoteDevice(bean.Address);
                    mClientThread = new ClientThread();
                    mClientThread.start();
                }
                dialog.cancel();
            }
        });
        stopDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothActivity.BlueToothAddress = null;
                dialog.cancel();
            }
        });
        stopDialog.show();
    }

    @Override
    public void OnclickMatching(DeviceBean item) {
        mBtnSearch.setText("正在匹配");
    }


    //得到配对的设备列表，清除已配对的设备
    public void removePairDevice(){
        if(mBluetoothAdapter!=null){
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : bondedDevices ){
                unpairDevice(device);
            }
        }

    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消搜索
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        //注销广播
        this.unregisterReceiver(mReceiver);

        if (BluetoothActivity.mType == BluetoothActivity.Type.CILENT) {
            shutdownClient();
        } else if (BluetoothActivity.mType == BluetoothActivity.Type.SERVICE) {
            shutdownServer();
        }
        BluetoothActivity.isOpen = false;
        BluetoothActivity.mType = BluetoothActivity.Type.NONE;


        //停止服务
        shutdownClient();
        shutdownServer();
    }
}
