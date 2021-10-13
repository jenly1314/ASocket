package com.king.asocket.tcp;

import com.king.asocket.ISocket;
import com.king.asocket.util.LogUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * TCP客户端
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class TCPClient implements ISocket<Socket> {

    private final Object mLock = new Object();

    private Socket mSocket;

    private String mHost;

    private int mPort;

    private volatile boolean isStart;

    private OutputStream mOutputStream;

    private InputStream mInputStream;

    private OnSocketStateListener mOnSocketStateListener;
    private OnMessageReceivedListener mOnMessageReceivedListener;

    private Executor mExecutor;

    /**
     * 构造
     * @param host TCP服务端的主机地址
     * @param port TCP服务端的端口
     */
    public TCPClient(String host,int port){
        mHost = host;
        mPort = port;
    }

    @Override
    public Socket getSocket(){
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
    public Socket createSocket() throws Exception {
        Socket socket = new Socket();
        socket.setKeepAlive(true);
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
                    mSocket.connect(new InetSocketAddress(mHost,mPort),10000);
                    LogUtils.d(String.format("localAddress:%s:%d",mSocket.getLocalAddress().getHostAddress(),mSocket.getLocalPort()));
                    LogUtils.d(String.format("Connect to %s:%d",mHost,mPort));
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onStarted();
                    }
                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();
                    while (isStart() && !mSocket.isInputShutdown()){
                        int len = mInputStream.available();
                        if(len > 0){
                            byte[] data = new byte[len];
                            int ret = mInputStream.read(data);
                            if(ret != -1){
                                if(LogUtils.isShowLog()){
                                    LogUtils.d("Received:"  + new String(data));
                                }
                                if(mOnMessageReceivedListener != null){
                                    mOnMessageReceivedListener.onMessageReceived(data);
                                }
                            }
                        }
                    }
                    mInputStream.close();
                    mOutputStream.close();
                    close();
                    LogUtils.d("TCPClient close.");
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
        try {
            isStart = false;
            if(!isClosed()){
                mSocket.close();
            }
        } catch (Exception e) {
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

    @Override
    public void write(byte[] data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        if(!mSocket.isOutputShutdown()){
            try {
                mOutputStream.write(data);
                mOutputStream.flush();
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
    }

    /**
     * TCPClient 请使用 {@link #write(byte[])}
     * @param data
     */
    @Override
    public void write(DatagramPacket data) {
        byte[] value = new byte[data.getLength() - data.getOffset()];
        System.arraycopy(data.getData(),data.getOffset(),value,0,value.length);
        write(value);
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
