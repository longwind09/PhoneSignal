
package com.example.phonesignal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import socket.longwind.MinaTcpClient;
import socket.longwind.MinaUdpClient;
import vrmsg.WifiMessage;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

//import com.unity3d.player.UnityPlayer;

public class MainActivity extends Activity
{

	private Button btn_con;

	private Button btn_sam;

	private Button btn_loc;

	private Button btn_refresh;

	private Button btn_disconn;

	private Button btn_stop;

	private EditText edt_x;

	private EditText edt_y;

	private EditText edt_z;

	private EditText edt_times;

	private EditText edt_interval;

	private TextView tv_pos;

	private Button connectButton = null;

	private Button sendButton = null;

	private Spinner spin_server;

	private MinaTcpClient tcpClient = null;

	private Handler mHandler = null;

	private boolean stop = true;

	public WifiManager wifiManager; //

	public ConnectivityManager connectManager;

	public static final String CELL_ID = android.os.Build.MODEL;

	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
	        "yyyy-MM-dd HH:mm:ss ");

	private MinaUdpClient udpClient = null;

	private List<String> serverList = new ArrayList<String>();

	public static String tcpServerIp;

	public WifiManager.MulticastLock lock = null;

	private boolean thread_runable = false;

	private NumberPicker pos_picker = null;

	private int posNum = 10;

	private String[] pos_nums;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		        .detectDiskReads().detectDiskWrites().detectNetwork() // or
		                                                              // .detectAll()
		                                                              // for all
		                                                              // detectable
		                                                              // problems
		        .penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		        .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
		        .penaltyLog().penaltyDeath().build());

		btn_con = (Button) findViewById(R.id.btn_conn);
		btn_sam = (Button) findViewById(R.id.btn_sample);

		btn_refresh = (Button) findViewById(R.id.btn_refresh);
		btn_disconn = (Button) findViewById(R.id.btn_disconn);
		btn_loc = (Button) findViewById(R.id.btn_location);

		btn_stop = (Button) findViewById(R.id.btn_stop);
		edt_x = (EditText) findViewById(R.id.edt_x);
		edt_y = (EditText) findViewById(R.id.edt_y);
		edt_y.setText("0");
		edt_y.setEnabled(false);
		edt_z = (EditText) findViewById(R.id.edt_z);

		edt_times = (EditText) findViewById(R.id.edt_times);
		edt_interval = (EditText) findViewById(R.id.edt_interval);

		tv_pos = (TextView) findViewById(R.id.tv_pos);
		spin_server = (Spinner) findViewById(R.id.spin_server);

		pos_picker = (NumberPicker) findViewById(R.id.np_posNum);
		pos_picker.setMinValue(1);
		pos_picker.setMaxValue(18);

		Resources res = getResources();
		pos_nums = res.getStringArray(R.array.pos_nums);

		spin_server.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
			        int position, long id)
			{

				String str = parent.getItemAtPosition(position).toString();
				tcpServerIp = str;
				Toast.makeText(MainActivity.this, "你点击的是:" + str, 2000).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

				// TODO Auto-generated method stub

			}
		});
		btn_con.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				tcpClient.connect(tcpServerIp, 9999);
			}
		});

		btn_disconn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				tcpClient.disConnect();
			}
		});
		btn_sam.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				if (getInputPosition() == null)
				{
					displayToast("please input the right double type number!");
					return;
				}
				startThread(((Button) v).getId());
				// minaClient.send(getWlanInfo());
			}

		});
		btn_loc.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				// minaClient.send(generateQueryPositionRequest());
				startThread(((Button) v).getId());
			}

		});
		btn_stop.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				// lock.acquire();
				stopThread();
			}

		});
		btn_refresh.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				// lock.acquire();
				udpClient.start();
			}

		});

		pos_picker.setOnValueChangedListener(new OnValueChangeListener()
		{

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
			        int newVal)
			{

				// TODO Auto-generated method stub
				posNum = newVal;
				setInputPosition();
			}

		});

		mHandler = new Handler()
		{

			@Override
			public void handleMessage(Message msg)
			{

				if (msg == null) return;
				switch (msg.what)
				{
				// 返回坐标
					case 1:
						tv_pos.setText((msg.obj).toString());
						break;
					// 发现服务器
					case 3:
						bindSpinner(msg.obj.toString());
						// lock.release();
						break;
					case 4:
						handleConnect(msg);
						break;
					case 5:
						handleDisConnect(msg);
						break;
					// 剩余采样次数
					case 20:
						edt_times.setText(msg.obj.toString());
						break;
					case 30:
						handleThreadFinished();
						break;
					default:
						displayToast(msg.obj.toString());
						break;
				}
			}
		};

		udpClient = new MinaUdpClient(getApplicationContext(), mHandler);

		tcpClient = new MinaTcpClient(getApplicationContext(), mHandler);

		initialUIState();
	}

	/**
	 * @param i
	 * 
	 */
	private void startThread(int buttonId)
	{

		int times = 0;
		double interval = 0;
		try
		{
			times = Integer.parseInt(edt_times.getText().toString());
			interval = Double.parseDouble(edt_interval.getText().toString());
			if (times < 0 || interval < 0)
			{
				displayToast("can not be negtive!");
				return;
			}
		}
		catch (Exception e)
		{
			displayToast("please input right times and interval!");
			return;
		}
		thread_runable = true;
		Thread aThread = new SampleThread(buttonId, times, interval);

		handleThreading();
		aThread.start();
	}

	protected void stopThread()
	{

		// TODO Auto-generated method stub
		thread_runable = false;
	}

	private void initialUIState()
	{

		btn_disconn.setEnabled(false);
		btn_sam.setEnabled(false);
		btn_loc.setEnabled(false);
		btn_stop.setEnabled(false);
		pos_picker.setEnabled(false);
	}

	protected void handleDisConnect(Message msg)
	{

		// TODO Auto-generated method stub
		btn_con.setEnabled(true);
		btn_sam.setEnabled(false);
		btn_loc.setEnabled(false);
		btn_disconn.setEnabled(false);
		btn_stop.setEnabled(false);
		pos_picker.setEnabled(false);
		stopThread();
	}

	protected void handleConnect(Message msg)
	{

		btn_con.setEnabled(false);
		btn_disconn.setEnabled(true);
		btn_sam.setEnabled(true);
		btn_loc.setEnabled(true);
		pos_picker.setEnabled(true);
	}

	protected void handleThreading()
	{

		btn_sam.setEnabled(false);
		btn_loc.setEnabled(false);
		pos_picker.setEnabled(false);
	}

	protected void handleThreadFinished()
	{

		btn_stop.setEnabled(false);
		btn_sam.setEnabled(true);
		btn_loc.setEnabled(true);
		pos_picker.setEnabled(true);
	}

	// protected void handleSampling()
	// {
	//
	// pos_picker.setEnabled(true);
	// }
	//
	// protected void handlePositioning()
	// {
	//
	// btn_con.setEnabled(false);
	// btn_disconn.setEnabled(true);
	// btn_stop.setEnabled(true);
	// btn_sam.setEnabled(true);
	// btn_loc.setEnabled(true);
	// pos_picker.setEnabled(true);
	// }

	protected void bindSpinner(String server)
	{

		if (serverList.contains(server)) return;
		serverList.add(server);
		String[] serverStrings = (String[]) serverList
		        .toArray(new String[serverList.size()]);
		ArrayAdapter<String> _Adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_item, serverStrings);
		spin_server.setAdapter(_Adapter);
		tcpServerIp = spin_server.getItemAtPosition(0).toString();
	}

	private void displayToast(String s)
	{

		Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy()
	{

		super.onDestroy();

	}

	@Override
	protected void onResume()
	{

		super.onResume();
	}

	@Override
	protected void onPause()
	{

		super.onPause();
		// unregisterReceiver(wifiIntentReceiver);
	}

	public WifiMessage.AddWifiSignalRequest getWlanInfo()
	{

		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String nowtime = dateFormatter.format(curDate);
		WifiMessage.Vector3 currentPosition = getInputPosition();

		WifiMessage.AddWifiSignalRequest.Builder addBuilder = WifiMessage.AddWifiSignalRequest
		        .newBuilder();
		wifiManager.startScan();
		ArrayList<ScanResult> list = (ArrayList<ScanResult>) wifiManager
		        .getScanResults();
		int _count = list.size();

		for (int i = 0; i < _count; i++)
		{

			String _bssid = list.get(i).BSSID;
			int _level = list.get(i).level;
			String _ssid = list.get(i).SSID;

			WifiMessage.WifiSignal.Builder sigBuilder = WifiMessage.WifiSignal
			        .newBuilder();
			sigBuilder.setDevice(CELL_ID);
			sigBuilder.setId(_bssid);
			sigBuilder.setIntensity(_level);
			sigBuilder.setMyPos(currentPosition);
			sigBuilder.setName(_ssid);
			sigBuilder.setNowtime(nowtime);
			addBuilder.addSignals(sigBuilder);
		}
		return addBuilder.build();
	}

	// 一次定位请求
	public WifiMessage.QueryPostionRequest generateQueryPositionRequest()
	{

		HashMultimap<String, Integer> mmap = HashMultimap.create();
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String nowtime = dateFormatter.format(curDate);
		WifiMessage.QueryPostionRequest.Builder queryBuilder = WifiMessage.QueryPostionRequest
		        .newBuilder();

		wifiManager.startScan();
		ArrayList<ScanResult> list = (ArrayList<ScanResult>) wifiManager
		        .getScanResults();
		int _count = list.size();

		for (int i = 0; i < _count; i++)
		{

			String _bssid = list.get(i).BSSID;
			int _level = list.get(i).level;
			String _ssid = list.get(i).SSID;

			WifiMessage.WifiSignal.Builder sigBuilder = WifiMessage.WifiSignal
			        .newBuilder();
			sigBuilder.setDevice(CELL_ID);
			sigBuilder.setId(_bssid);
			sigBuilder.setIntensity(_level);
			// sigBuilder.setMyPos(getInputPosition());
			sigBuilder.setName(_ssid);
			sigBuilder.setNowtime(nowtime);
			queryBuilder.addSignals(sigBuilder);
		}
		return queryBuilder.build();
	}

	/**
	 * @param handler
	 * @param code
	 * @param arg0
	 * @param arg1
	 * @param obj
	 */
	public static void sendMessageToUI(Handler handler, int code, int arg0,
	        int arg1, Object obj)
	{

		Message msg = handler.obtainMessage(code, arg0, arg0, obj);
		handler.sendMessage(msg);
	}

	// 自定义线程，负责多次采样后的定位
	class SampleThread extends Thread
	{

		public int times;

		public double interval;

		public int buttonId;

		public SampleThread(int buttonId, int times, double interval)
		{

			this.buttonId = buttonId;
			this.times = times;
			this.interval = interval;

		}

		@Override
		public void run()
		{

			switch (buttonId)
			{
				case R.id.btn_sample:
					doSample();
					break;
				case R.id.btn_location:
					doLocation();
					break;
				default:
					break;
			}

			sendMessageToUI(mHandler, 30, 1, 1, "threadState");
		}

		private void doSample()
		{

			for (int i = 0; i < times; i++)
			{
				if (!thread_runable) return;
				tcpClient.send(getWlanInfo());
				try
				{
					Thread.sleep((long) (interval * 1000));
					sendMessageToUI(mHandler, 20, 1, 1, times - i - 1);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
         * 
         */
		private void doLocation()
		{

			ArrayListMultimap<String, Integer> mmap = ArrayListMultimap
			        .create();
			HashMap<String, Integer> key_count_map = new HashMap<String, Integer>();
			for (int j = 0; j < times; j++)
			{

				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String nowtime = dateFormatter.format(curDate);
				wifiManager.startScan();
				ArrayList<ScanResult> list = (ArrayList<ScanResult>) wifiManager
				        .getScanResults();
				int _count = list.size();
				for (int i = 0; i < _count; i++)
				{
					if (!thread_runable) return;
					String _bssid = list.get(i).BSSID;
					int _level = list.get(i).level;
					mmap.put(_bssid, _level);
					if (key_count_map.containsKey(_bssid))
					{
						int count = key_count_map.get(_bssid);
						key_count_map.put(_bssid, count + 1);
					}
					else
					{
						key_count_map.put(_bssid, 1);
					}
				}
				try
				{
					Thread.sleep((long) (interval * 1000));
					Message msg = mHandler.obtainMessage(20, 1, 1, times - j
					        - 1);
					mHandler.sendMessage(msg);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// get the maximume frequency of the aps
			Set<Entry<String, Integer>> sets = key_count_map.entrySet();
			int max = 0;
			for (Entry<String, Integer> entry : sets)
			{
				int value = entry.getValue();

				if (value > max)
				{
					max = value;
				}
			}

			WifiMessage.QueryPostionRequest.Builder queryBuilder = WifiMessage.QueryPostionRequest
			        .newBuilder();
			// filtering
			for (Entry<String, Integer> entry : sets)
			{
				if (!thread_runable) return;
				// remove those show up less than half of the max
				String key = entry.getKey();
				int value = entry.getValue();
				if (value < max / 2) continue;

				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String nowtime = dateFormatter.format(curDate);
				//
				List<Integer> valueSet = mmap.get(key);
				Integer[] valueArray = (Integer[]) valueSet
				        .toArray(new Integer[valueSet.size()]);
				Arrays.sort(valueArray);
				int median = valueArray[valueSet.size() / 2];
				// String _ssid = list.get(i).SSID;

				WifiMessage.WifiSignal.Builder sigBuilder = WifiMessage.WifiSignal
				        .newBuilder();
				sigBuilder.setDevice(CELL_ID);
				sigBuilder.setId(key);
				sigBuilder.setIntensity(median);
				sigBuilder.setMyPos(getInputPosition());
				// sigBuilder.setName(_ssid);
				sigBuilder.setNowtime(nowtime);
				queryBuilder.addSignals(sigBuilder);
			}
			tcpClient.send(queryBuilder.build());
		}
	}

	public WifiMessage.Vector3 getInputPosition()
	{

		double x = 0;
		double y = 0;
		double z = 0;
		try
		{
			x = Double.valueOf(edt_x.getText().toString());
			y = Double.valueOf(edt_y.getText().toString());
			z = Double.valueOf(edt_z.getText().toString());
		}
		catch (Exception e)
		{
			// TODO: handle exception
			return null;
		}

		WifiMessage.Vector3.Builder builder = WifiMessage.Vector3.newBuilder();
		builder.setX(x);
		builder.setY(y);
		builder.setZ(z);
		return builder.build();
	}

	public void setInputPosition()
	{

		if (pos_nums.length < 1) return;
		if (posNum < 1 || posNum > 18) return;
		String posString = pos_nums[posNum - 1];
		String pos[] = posString.split(",");
		if (pos.length != 3) return;

		edt_x.setText(pos[0]);
		edt_y.setText(pos[1]);
		edt_z.setText(pos[2]);
		// double x = 0;
		// double y = 0;
		// double z = 0;
		// try
		// {
		// x = Double.valueOf(pos[0]);
		// y = Double.valueOf(pos[1]);
		// z = Double.valueOf(pos[2]);
		//
		// }
		// catch (Exception e)
		// {
		// // TODO: handle exception
		// return;
		// }

	}

}
