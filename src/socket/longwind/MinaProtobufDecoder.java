
package socket.longwind;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import android.util.Log;

public class MinaProtobufDecoder extends CumulativeProtocolDecoder
{

	public final static int kHeaderLen = 4;

	public final static int kMessageNameLen = 2;

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
	        ProtocolDecoderOutput out) throws Exception
	{

		// 如果没有接收完Header部分（4字节），直接返回false
		if (in.remaining() < kHeaderLen)
		{
			return false;
		}
		else
		{

			// 标记开始位置，如果一条消息没传输完成则返回到这个位置
			in.mark();
			// 读取header部分，获取body长度
			byte[] bodyLenBytes = new byte[kHeaderLen];
			in.get(bodyLenBytes);
			String bodyLenString = new String(bodyLenBytes);
			int bodyLength = Integer.valueOf(bodyLenString, 16);

			System.out.println(bodyLength);
			// 如果body没有接收完整，直接返回false
			if (in.remaining() < bodyLength)
			{
				in.reset(); // IoBuffer position回到原来标记的地方
				return false;
			}
			else
			{
				byte[] typeLenBytes = new byte[kMessageNameLen];
				in.get(typeLenBytes);
				int typeLen = Integer.valueOf(new String(typeLenBytes), 16);

				if (in.remaining() < typeLen)
				{
					in.reset(); // IoBuffer position回到原来标记的地方
					return false;
				}

				byte[] typeBytes = new byte[typeLen];
				in.get(typeBytes);
				String cpp_typeString = new String(typeBytes);
				String[] arr_strStrings = cpp_typeString.split("\\.");
				String java_typeString = arr_strStrings[arr_strStrings.length - 1];
				String msgType = "vrmsg.WifiMessage$" + java_typeString;
				System.out.println(msgType);
				Log.v("type", msgType);
				// String msgType = new String(typeBytes);
				int msglen = bodyLength - kMessageNameLen - typeLen;
				if (in.remaining() < msglen)
				{
					in.reset(); // IoBuffer position回到原来标记的地方
					return false;
				}
				byte[] bodyBytes = new byte[msglen];
				in.get(bodyBytes); // 读取body部分

				Object object = parseDynamic(msgType, bodyBytes);
				out.write(object); // 解析出一条消息
				return true;
			}
		}
	}

	private Object parseDynamic(String type, byte[] bytes)
	{

		try
		{
			Class<?> clazz = null;
			try
			{
				clazz = Class.forName(type);
			}
			catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Method method = clazz.getDeclaredMethod("parseFrom", byte[].class);
			return method.invoke(null, bytes);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Non-message type", e);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalArgumentException("Non-message type", e);
		}
		catch (InvocationTargetException e)
		{
			// TODO: Work out what exactly you want to do.
			throw new IllegalArgumentException("Bad data?", e);
		}
	}
}
