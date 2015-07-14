
package socket.longwind;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import vrmsg.WifiMessage;

public class MinaTcpClient
{

	private NioSocketConnector connector;

	public NioSocketConnector getConnector()
	{

		return connector;
	}

	public void setConnector(NioSocketConnector connector)
	{

		this.connector = connector;
	}

	public ConnectFuture getCf()
	{

		return cf;
	}

	public void setCf(ConnectFuture cf)
	{

		this.cf = cf;
	}

	private ConnectFuture cf;

	private Context applicationContext;

	public MinaTcpClient(Handler mHandler)
	{

		connector = new NioSocketConnector();
		// 指定protobuf的编码器和解码器
		connector.getFilterChain().addLast(
		        "codec",
		        new ProtocolCodecFilter(new MinaProtobufEncoder(),
		                new MinaProtobufDecoder()));

		connector.setConnectTimeoutMillis(3000);

		connector.setHandler(new TcpClientHandler(mHandler));// 设置事件处理器
	}

	public MinaTcpClient(Context applicationContext, Handler mHandler)
	{

		this.applicationContext = applicationContext;

		connector = new NioSocketConnector();
		// 指定protobuf的编码器和解码器
		connector.getFilterChain().addLast(
		        "codec",
		        new ProtocolCodecFilter(new MinaProtobufEncoder(),
		                new MinaProtobufDecoder()));

		connector.setConnectTimeoutMillis(3000);

		connector.setHandler(new TcpClientHandler(mHandler));// 设置事件处理器
	}

	private void displayToast(String s)
	{

		Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show();
	}

	public void connect(String serverAddress, int port)
	{

		// 创建客户端连接器.

		// connector.getFilterChain().addLast("logger", new LoggingFilter());
		// connector.getFilterChain().addLast( "codec", new ProtocolCodecFilter(
		// new TextLineCodecFactory( Charset.forName( "UTF-8" )))); //设置编码过滤器
		// connector.getFilterChain().addLast("codec",
		// new ProtocolCodecFilter(new TextLineCodecFactory()));

		cf = connector.connect(new InetSocketAddress(serverAddress, port));// 建立连接
		cf.join(4);
		// cf.awaitUninterruptibly();// 等待连接创建完成
		// cf.getSession().getCloseFuture().awaitUninterruptibly();// 等待连接断开
		// connector.dispose();
	}

	public void send(Object msg)
	{

		if (cf.isConnected())
		{
			cf.getSession().write(msg);
		}
	}

	public void disConnect()
	{

		cf.getSession().close(false);
		// connector.dispose();
	}
}
