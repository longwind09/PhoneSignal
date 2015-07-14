
package socket.longwind;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import vrmsg.WifiMessage;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author longwind
 * 
 *         1 vrmsg.wifimessage.querypositionrequest 2 msgclosed
 * 
 * 
 */
public class TcpClientHandler extends IoHandlerAdapter
{

	Handler mHandler;

	public TcpClientHandler(Handler mHandler)
	{

		this.mHandler = mHandler;
	}

	@Override
	public void sessionCreated(IoSession session)
	{

		// 显示客户端的ip和端口
		Message msg = mHandler.obtainMessage(4, 1, 1, "connected");
		mHandler.sendMessage(msg);
	}

	@Override
	public void sessionClosed(IoSession session)
	{

		String msgClosed = "session closed";
		Message msg = mHandler.obtainMessage(5, 1, 1, msgClosed);
		mHandler.sendMessage(msg);
	}

	@Override
	public void messageSent(IoSession session, Object message)
	{

	}

	@Override
	public void messageReceived(IoSession session, Object message)
	        throws Exception
	{

		Log.v("phoneSignal", "message received");
		WifiMessage.QueryPostionResponse response = (WifiMessage.QueryPostionResponse) message;
		WifiMessage.Vector3 vec = response.getPostions(0);
		Log.v("vec", vec.toString());
		Message msg = mHandler.obtainMessage(1, 1, 1, vec);
		mHandler.sendMessage(msg);
		// WriteFuture future = session.write(v);
		// future.addListener(new IoFutureListener<WriteFuture>()
		// {
		//
		// // write操作完成后调用的回调函数
		// public void operationComplete(WriteFuture future)
		// {
		//
		// if (future.isWritten())
		// {
		// Log.v("phoneSignal", "write操作成功");
		// }
		// else
		// {
		// Log.v("phoneSignal", "write操作失败");
		// }
		// }
		// });
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
	        throws Exception
	{

		Log.v("phoneSignal", "session exception");
		String msgClosed = "session exception";
		Message msg = mHandler.obtainMessage(5, 1, 1, msgClosed);
		mHandler.sendMessage(msg);
	}
}
