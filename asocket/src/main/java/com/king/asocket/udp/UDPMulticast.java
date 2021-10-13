package com.king.asocket.udp;

import com.king.asocket.ISocket;
import com.king.asocket.util.LogUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * UDP组播
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class UDPMulticast implements ISocket<MulticastSocket> {

    private final Object mLock = new Object();

    private MulticastSocket mSocket;

    private String mHost;

    private int mPort;

    private InetAddress mInetAddress;

    private int mLength;

    private boolean isStart;

    private OnSocketStateListener mOnSocketStateListener;
    private OnMessageReceivedListener mOnMessageReceivedListener;

    private Executor mExecutor;

    /**
     * 构造
     * @param host 组播地址
     * @param port 组播端口
     */
    public UDPMulticast(String host, int port){
        this(host,port,1460);
    }

    /**
     * 构造
     * @param host 组播地址
     * @param port 组播端口
     * @param length 接收数据包的长度，超出会造成拆分成多条数据包
     */
    public UDPMulticast(String host, int port, int length){
        mHost = host;
        mPort = port;
        mLength = length;
    }

    @Override
    public MulticastSocket getSocket(){
        return mSocket;
    }

    @Override
    public void setExecutor(Executor executor) {
        if(isStart){
            return;
        }
        this.mExecutor = executor;
    }

    private Executor obtainExecutor(){
        if(mExecutor == null){
            synchronized (mLock){
                if(mExecutor == null){
                    mExecutor = Executors.newSingleThreadExecutor();
                }
            }
        }
        return mExecutor;
    }

    @Override
    public MulticastSocket createSocket() throws Exception {
        MulticastSocket socket = new MulticastSocket(mPort);
        socket.setReuseAddress(true);
        return socket;
    }

    @Override
    public void start() {
        if(isStart()){
            return;
        }
        LogUtils.d("start...");
        isStart = true;
        obtainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = createSocket();
                    mInetAddress = InetAddress.getByName(mHost);
                    mPort = mSocket.getLocalPort();
                    LogUtils.d(String.format("localAddress:%s:%d",mSocket.getLocalAddress().getHostAddress(),mPort));
                    mSocket.joinGroup(mInetAddress);
                    LogUtils.d(String.format("Join group:%s:%d",mHost,mPort));
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onStarted();
                    }
                    while (isStart()){
                        DatagramPacket data = new DatagramPacket(new byte[mLength],mLength);
                        try {
                            mSocket.receive(data);
                        }catch (SocketException e){
                            if(isClosed()){
                                break;
                            }
                        }
                        byte[] value = new byte[data.getLength() - data.getOffset()];
                        System.arraycopy(data.getData(),data.getOffset(),value,0,value.length);
                        if(LogUtils.isShowLog()){
                            LogUtils.d("Received:"  + new String(value));
                        }
                        if(mOnMessageReceivedListener != null){
                            mOnMessageReceivedListener.onMessageReceived(value);
                        }
                    }
                    if(!isClosed()){
                        mSocket.leaveGroup(InetAddress.getByName(mHost));
                        LogUtils.d("Leave group:" + mHost + ":" + mPort);
                    }
                    close();
                    LogUtils.d("UDPMulticast close.");
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onClosed();
                    }
                } catch (Exception e) {
                    isStart = false;
                    LogUtils.w(e);
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onException(e);
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        try{
            isStart = false;
            if(!isClosed()){
                mSocket.close();
            }
        }catch (Exception e){
            LogUtils.w(e);
            if(mOnSocketStateListener != null){
                mOnSocketStateListener.onException(e);
            }
        }

    }

    @Override
    public boolean isStart() {
        return mSocket != null && isStart && !mSocket.isClosed();
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

    /**
     * 组播消息，默认发送至当前加入的组
     * @param data
     */
    @Override
    public void write(byte[] data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        try {
            if(mInetAddress == null){
                mInetAddress = InetAddress.getByName(mHost);
            }
            DatagramPacket packet = new DatagramPacket(data,0,data.length,mInetAddress,mPort);
            mSocket.send(packet);
            if(LogUtils.isShowLog()) {
                LogUtils.d("write:" + new String(data));
            }
        } catch (Exception e) {
            LogUtils.w(e);
            if(mOnSocketStateListener != null){
                mOnSocketStateListener.onException(e);
            }
        }
    }

    @Override
    public void write(DatagramPacket data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        try {
            mSocket.send(data);
            if(LogUtils.isShowLog()) {
                byte[] value = new byte[data.getLength() - data.getOffset()];
                System.arraycopy(data.getData(), data.getOffset(), value, 0, value.length);
                LogUtils.d("write:" + new String(value));
            }
        } catch (Exception e) {
            LogUtils.w(e);
            if(mOnSocketStateListener != null){
                mOnSocketStateListener.onException(e);
            }
        }
    }

    @Override
    public void setOnSocketStateListener(OnSocketStateListener listener) {
        mOnSocketStateListener = listener;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mOnMessageReceivedListener = listener;
    }
}