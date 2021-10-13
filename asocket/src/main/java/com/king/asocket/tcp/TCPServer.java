package com.king.asocket.tcp;

import com.king.asocket.ISocket;
import com.king.asocket.util.LogUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TCP服务端
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class TCPServer implements ISocket<ServerSocket> {

    private final Object mLock = new Object();

    private ServerSocket mServer;

    private int mBacklog;

    private int mPort;

    private volatile boolean isStart;

    private boolean isConnected;

    private Map<String,Socket> cacheClient;

    private OnSocketStateListener mOnSocketStateListener;
    private OnMessageReceivedListener mOnMessageReceivedListener;

    private Executor mExecutor;

    /**
     * 构造
     * @param port ServerSocket的端口
     */
    public TCPServer(int port){
        this(port,50);
    }

    /**
     * 构造
     * @param port ServerSocket的端口
     * @param backlog ServerSocket允许链接队列的最大长度
     */
    public TCPServer(int port,int backlog){
        mPort = port;
        mBacklog = backlog;
        cacheClient = new HashMap<>(mBacklog);
    }

    @Override
    public ServerSocket getSocket(){
        return mServer;
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
                    mExecutor = new ThreadPoolExecutor(5, mBacklog,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return mExecutor;
    }

    @Override
    public ServerSocket createSocket() throws Exception {
        ServerSocket serverSocket = new ServerSocket(mPort,mBacklog);
        serverSocket.setReuseAddress(true);
        return serverSocket;
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
                    mServer = createSocket();
                    mPort = mServer.getLocalPort();
                    isConnected = true;
                    LogUtils.d(String.format("localAddress:%s:%d",mServer.getInetAddress().getHostAddress(),mServer.getLocalPort()));
                    if(!cacheClient.isEmpty()){
                        cacheClient.clear();
                    }
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onStarted();
                    }
                    while (isStart()){
                        try{
                            Socket socket = mServer.accept();
                            processSocketTask(mExecutor,socket);
                        }catch (SocketException e){
                            if(isClosed()){
                                break;
                            }
                        }
                    }
                    close();
                    LogUtils.d("TCPServer close.");
                    if(mOnSocketStateListener != null){
                        mOnSocketStateListener.onClosed();
                    }
                } catch (Exception e) {
                    isConnected = false;
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
            cacheClient.clear();
            isConnected = false;
            isStart = false;
            if(!isClosed()){
                mServer.close();
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
        return mServer != null && isStart && !mServer.isClosed();
    }


    @Override
    public boolean isConnected() {
        return mServer != null && isConnected && mServer.isBound();
    }

    @Override
    public boolean isClosed() {
        if(mServer != null){
            return mServer.isClosed();
        }
        return true;
    }

    /**
     * TCPServer 当前表示群发
     * @param data
     */
    @Override
    public void write(byte[] data) {
        if(!isStart()){
            LogUtils.d("Client has not started");
            return;
        }
        for(Socket socket: cacheClient.values()){
            if(!socket.isOutputShutdown()){
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
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
    }

    /**
     * TCPServer 当前表示发送到指定 Address，如果不指定，将使用群发
     * @param data
     */
    @Override
    public void write(DatagramPacket data) {
        byte[] value = new byte[data.getLength() - data.getOffset()];
        if (data.getAddress() != null) {
            String host = data.getAddress().getHostAddress();
            int port = data.getPort();
            String key = String.format("%s:%d", host, port);
            Socket socket = cacheClient.get(key);
            if (socket != null) {
                if(!isStart()){
                    LogUtils.d("Client has not started");
                    return;
                }
                System.arraycopy(data.getData(), data.getOffset(), value, 0, value.length);
                if (!socket.isOutputShutdown()) {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(value);
                        outputStream.flush();
                        if(LogUtils.isShowLog()) {
                            LogUtils.d("write:" + new String(value));
                        }
                    } catch (Exception e) {
                        LogUtils.w(e);
                        if(mOnSocketStateListener != null){
                            mOnSocketStateListener.onException(e);
                        }
                    }
                }
                return;
            }
        }

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

    private void processSocketTask(Executor executor, final Socket socket){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                processSocket(socket);
            }
        });
    }

    private void processSocket(Socket socket){
        try{
            String host = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            String key = String.format("%s:%d",host,port);
            cacheClient.put(key,socket);
            InputStream inputStream = socket.getInputStream();
            int len;
            while (isStart() && !socket.isInputShutdown()){
                len = inputStream.available();
                if(len > 0){
                    byte[] data = new byte[len];
                    int ret = inputStream.read(data);
                    if(ret != -1){
                        if(LogUtils.isShowLog()){
                            LogUtils.d("Received:" + new String(data));
                        }
                        if(mOnMessageReceivedListener != null){
                            mOnMessageReceivedListener.onMessageReceived(data);
                        }
                    }
                }
            }
            cacheClient.remove(key);
            inputStream.close();
            socket.close();
        }catch (Exception e){
            LogUtils.w(e);
            if(mOnSocketStateListener != null){
                mOnSocketStateListener.onException(e);
            }
        }
    }
}
