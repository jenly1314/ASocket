package com.king.asocket.tcp;

import com.king.asocket.ISocket;
import com.king.asocket.util.LogUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP客户端
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class TCPClient implements ISocket<Socket> {

    private Socket mSocket;

    private String mHost;

    private int mPort;

    private boolean isStart;

    private OutputStream mOutputStream;

    private InputStream mInputStream;

    private OnMessageReceivedListener mOnMessageReceivedListener;

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
    public void start() {
        if(isStart()){
            return;
        }
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(mHost,mPort));
            mSocket.setKeepAlive(true);
            isStart = mSocket.isBound();
            LogUtils.d(String.format("localAddress:%s:%d",mSocket.getLocalAddress().getHostAddress(),mSocket.getLocalPort()));
            LogUtils.d(String.format("Connect to %s:%d",mHost,mPort));
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
            mSocket.close();
        } catch (Exception e) {
            isStart = false;
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if(!isClosed()){
                mSocket.close();
            }
            isStart = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStart() {
        return mSocket != null && isStart && !mSocket.isClosed();
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
                e.printStackTrace();
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
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mOnMessageReceivedListener = listener;
    }
}
