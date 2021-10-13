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
import java.util.concurrent.Executor;


/**
 * ASocket 适用于 Android 的 Socket，方便快速实现TCP的长连接、UDP的单播、组播、广播等相关通信。
 *
 * 通过 ASocket 统一管理 TCP/UDP 相关 Socket，让其适用于 Android，在UI主线程调用和回调，在子线程异步处理消息的发送与接收
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class ASocket implements ISocket<Object>{

    private ISocket mSocket;

    private OnSocketStateListener mOnSocketStateListener;
    private OnMessageReceivedListener mOnMessageReceivedListener;

    private static final int WHAT_SEND_MESSAGE = 0x11;
    private static final int WHAT_RECEIVE_MESSAGE = 0x12;

    private static final int WHAT_STATE_STARTED = 0x21;
    private static final int WHAT_STATE_CLOSED = 0x22;
    private static final int WHAT_STATE_EXCEPTION = 0xFF;

    private static final int BYTE_DATA_MESSAGE = 0x01;
    private static final int DATAGRAM_PACKET_MESSAGE = 0x02;

    private Handler mMainHandler;

    private HandlerThread mHandlerThread;

    private Handler mWorkHandler;

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
        initHandler();
    }

    private void initHandler(){
        mHandlerThread = new HandlerThread("MessageThread");
        mHandlerThread.start();

        mWorkHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(mSocket == null){
                    return;
                }
                switch (msg.what){
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

        mMainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                LogUtils.d("handleMessage: 0x" + Long.toHexString(msg.what).toUpperCase());
                switch (msg.what){
                    case WHAT_RECEIVE_MESSAGE:
                        if(mOnMessageReceivedListener != null){
                            mOnMessageReceivedListener.onMessageReceived((byte[]) msg.obj);
                        }
                        break;
                    case WHAT_STATE_STARTED:
                        if(mOnSocketStateListener != null){
                            mOnSocketStateListener.onStarted();
                        }
                        break;
                    case WHAT_STATE_CLOSED:
                        if(mOnSocketStateListener != null){
                            mOnSocketStateListener.onClosed();
                        }
                        break;
                    case WHAT_STATE_EXCEPTION:
                        if(mOnSocketStateListener != null){
                            mOnSocketStateListener.onException((Exception) msg.obj);
                        }
                        break;
                }
            }
        };
    }



    @Override
    public Object getSocket() {
        if(mSocket != null){
            return mSocket.getSocket();
        }
        return null;
    }

    @Override
    public void setExecutor(Executor executor) {
        if(mSocket != null){
            mSocket.setExecutor(executor);
        }
    }

    @Override
    public Object createSocket() throws Exception {
        return mSocket.createSocket();
    }

    @Override
    public void start() {
        if(isStart()){
            return;
        }
        if(mOnSocketStateListener == null){
            setOnSocketStateListener(null);
        }
        if(mSocket != null){
            mSocket.start();
        }
    }

    @Override
    public void close() {
        if(mSocket != null){
            mSocket.close();
        }
    }

    /**
     * 关闭并退出，与 {@link #close() }类似，相对于{@link #close() } 的区别是会多一步退出线程消息队列操作；退出消息队列后将无法在发送消息。
     * 此方法一般用于在明确不再使用时调用。
     */
    public void closeAndQuit(){
        close();
        if(mHandlerThread != null){
            mHandlerThread.quit();
        }
    }

    @Override
    public boolean isStart() {
        return mSocket != null && mSocket.isStart();
    }

    @Override
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public boolean isClosed() {
        if(mSocket != null){
            return mSocket.isClosed();
        }
        return true;
    }

    @Override
    public void write(byte[] data) {
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
        mWorkHandler.obtainMessage(WHAT_SEND_MESSAGE,DATAGRAM_PACKET_MESSAGE, 0, data).sendToTarget();
    }

    @Override
    public void setOnSocketStateListener(OnSocketStateListener listener) {
        mOnSocketStateListener = listener;
        mSocket.setOnSocketStateListener(new OnSocketStateListener() {
            @Override
            public void onStarted() {
                mMainHandler.sendEmptyMessage(WHAT_STATE_STARTED);
            }

            @Override
            public void onClosed() {
                mMainHandler.sendEmptyMessage(WHAT_STATE_CLOSED);
            }

            @Override
            public void onException(Exception e) {
                mMainHandler.obtainMessage(WHAT_STATE_EXCEPTION, e).sendToTarget();
            }
        });
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mOnMessageReceivedListener = listener;
        mSocket.setOnMessageReceivedListener(new OnMessageReceivedListener() {

            @Override
            public void onMessageReceived(byte[] data) {
                mMainHandler.obtainMessage(WHAT_RECEIVE_MESSAGE, data).sendToTarget();
            }
        });
    }



}
