package com.king.asocket;

import com.king.asocket.tcp.TCPClient;
import com.king.asocket.tcp.TCPServer;
import com.king.asocket.udp.UDPMulticast;
import com.king.asocket.udp.UDPClient;
import com.king.asocket.udp.UDPServer;
import com.king.asocket.util.LogUtils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.net.DatagramPacket;


/**
 * ASocket 适用于 Android 的 Socket，方便快速实现TCP的长连接、UDP的单播、组播、广播等相关通信。
 *
 * 通过 ASocket 统一管理 TCP/UDP 相关 Socket，让其适用于 Android，在 UI主线程调用，在子线程处理发送消息
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class ASocket implements ISocket<Object>{

    private ISocket mSocket;

    private OnMessageReceivedListener mOnMessageReceivedListener;


    private static final int WHAT_START_SOCKET = 0x01;
    private static final int WHAT_CLOSE_SOCKET = 0x02;

    private static final int WHAT_SEND_MESSAGE = 0x03;
    private static final int WHAT_RECEIVE_MESSAGE = 0x05;

    private static final int BYTE_DATA_MESSAGE =  0x01;
    private static final int DATAGRAM_PACKET_MESSAGE =  0x02;


    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == WHAT_RECEIVE_MESSAGE){
                if(mOnMessageReceivedListener != null){
                    mOnMessageReceivedListener.onMessageReceived((byte[]) msg.obj);
                }
            }
        }
    };

    private HandlerThread mHandlerThread = new HandlerThread("MessageThread");

    private Handler mWorkHandler = new Handler(mHandlerThread.getLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mSocket == null){
                return;
            }
            switch (msg.what){
                case WHAT_START_SOCKET:
                    mSocket.start();
                    break;
                case WHAT_CLOSE_SOCKET:
                    mSocket.close();
                    if(mHandlerThread != null){
                        mHandlerThread.quit();
                    }
                    break;
                case WHAT_SEND_MESSAGE:
                    if(msg.arg1 == BYTE_DATA_MESSAGE){
                        mSocket.write((byte[]) msg.obj);
                    }else if(msg.arg1 == DATAGRAM_PACKET_MESSAGE){
                        mSocket.write((DatagramPacket) msg.obj);
                    }
                    break;
            }
        }
    };



    /**
     * 构造
     *
     * @see {@link ISocket} 的实现类：
     * {@link TCPClient},
     * {@link TCPServer},
     * {@link UDPMulticast},
     * {@link UDPClient},
     * {@link UDPServer}
     *
     * @param socket {@link ISocket}
     */
    public ASocket(ISocket socket){
        this.mSocket = socket;
        mHandlerThread.start();
    }



    @Override
    public Object getSocket() {
        if(mSocket != null){
            return mSocket.getSocket();
        }
        return null;
    }

    @Override
    public void start() {
        if(isStart()){
            return;
        }
        mWorkHandler.obtainMessage(WHAT_START_SOCKET).sendToTarget();
    }


    @Override
    public void close() {
        if(!isClosed()){
            return;
        }
        mWorkHandler.obtainMessage(WHAT_CLOSE_SOCKET).sendToTarget();
    }

    @Override
    public boolean isStart() {
        return mSocket != null && mSocket.isStart();
    }

    @Override
    public boolean isClosed() {
        if(mSocket != null){
            return mSocket.isClosed();
        }
        return true;
    }

    @Override
    public void write(final byte[] data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        mWorkHandler.obtainMessage(WHAT_SEND_MESSAGE,BYTE_DATA_MESSAGE, 0, data).sendToTarget();
    }

    @Override
    public void write(DatagramPacket data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        mWorkHandler.obtainMessage(WHAT_SEND_MESSAGE,DATAGRAM_PACKET_MESSAGE, 0,data).sendToTarget();
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mOnMessageReceivedListener = listener;
        mSocket.setOnMessageReceivedListener(new OnMessageReceivedListener() {

            @Override
            public void onMessageReceived(byte[] data) {
                mMainHandler.obtainMessage(WHAT_RECEIVE_MESSAGE,data).sendToTarget();
            }
        });
    }

}
