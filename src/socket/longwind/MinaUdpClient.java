/**
 * a udp client with mina framework
 */

package socket.longwind;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * @author longwind
 * 
 */
public class MinaUdpClient
{

	public MinaUdpClient(Context context, Handler mHandler)
	{

		this.mHandler = mHandler;
	}

	public void start()
	{

		MyThread aThread = new MyThread();
		aThread.start();

	}

	private class MyThread extends Thread
	{

		public MyThread()
		{

		}

		@Override
		public void run()
		{

			byte[] buf = new byte[32];
			try
			{
				DatagramSocket ds = new DatagramSocket();
				ds.setSoTimeout(1000);
				DatagramPacket dp = new DatagramPacket(buf, buf.length,
				        InetAddress.getByName(HOST_STRING), UDP_SERVER_PORT);
				ds.send(dp);
				ds.receive(dp);
				String serverIpString = new String(dp.getAddress()
				        .getHostAddress());
				Message msgMessage = mHandler.obtainMessage(3, 1, 1,
				        serverIpString);
				mHandler.sendMessage(msgMessage);
				ds.disconnect();
				ds.close();

			}
			catch (SocketException e3)
			{
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private Handler mHandler = null;

	public static final int UDP_SERVER_PORT = 8888;

	public static final String HOST_STRING = "255.255.255.255";
}
