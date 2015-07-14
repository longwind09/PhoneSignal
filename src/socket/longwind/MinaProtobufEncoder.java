
package socket.longwind;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import android.util.Log;

import com.google.protobuf.Message;

public class MinaProtobufEncoder extends ProtocolEncoderAdapter
{

	public final static int kHeaderLen = 4;

	public final static int kMessageNameLen = 2;

	public void encode(IoSession session, Object message,
	        ProtocolEncoderOutput out) throws Exception
	{

		// WifiMessage.Vector3 vec = (WifiMessage.Vector3) message;
		// byte[] bytes = vec.toByteArray(); // Student对象转为protobuf字节码
		// int length = bytes.length;
		//
		// IoBuffer buffer = IoBuffer.allocate(length + 4);
		// buffer.putInt(length); // write header
		// buffer.put(bytes); // write body
		// buffer.flip();
		// out.write(buffer);

		Message msg = (Message) message;
		byte[] msgBytes = msg.toByteArray(); // Student对象转为protobuf字节码
		int msgLen = msgBytes.length;
		String msgType = "vrmsg." + msg.getClass().getSimpleName();
		byte[] typeBytes = msgType.getBytes();
		int typeLen = typeBytes.length;
		int bodyLen = kMessageNameLen + typeLen + msgLen;
		String hex_bodyLen = String.format("%04x", bodyLen);
		String hex_typeLen = String.format("%02x", typeLen);
		// dynamic allocate the volume of the message
		IoBuffer buffer = IoBuffer.allocate(kHeaderLen + bodyLen);
		// header:bodylen
		buffer.put(hex_bodyLen.getBytes());
		// type len
		buffer.put(hex_typeLen.getBytes());
		// write type
		buffer.put(typeBytes);
		// write body
		buffer.put(msgBytes);
		buffer.flip();
		out.write(buffer);
		Log.v("hearder", hex_bodyLen);
	}
}
