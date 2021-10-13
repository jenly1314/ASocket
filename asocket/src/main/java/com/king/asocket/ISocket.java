package com.king.asocket;

import com.king.asocket.tcp.TCPClient;
import com.king.asocket.tcp.TCPServer;
import com.king.asocket.udp.UDPMulticast;
import com.king.asocket.udp.UDPClient;
import com.king.asocket.udp.UDPServer;

import java.net.DatagramPacket;
import java.util.concurrent.Executor;

/**
 * ISocket接口
 *
 * @see {@link ISocket} 的相关实现类 {@link TCPClient},{@link TCPServer},
 * {@link UDPMulticast},{@link UDPClient},{@link UDPServer}
 *
 * @param <T> T表示原始的Socket对象
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public interface ISocket<T> {


    /**
     * 创建 Socket，主要是便于扩展和修改配置，仅供其子类内部调用，外部请勿直接调用
     * @return
     */
    T createSocket() throws Exception;

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void close();

    /**
     * 是否启动
     * @return
     */
    boolean isStart();

    /**
     * 是否已经连接
     * @return
     */
    boolean isConnected();

    /**
     * 是否已经关闭
     * @return
     */
    boolean isClosed();

    /**
     * 写入数据
     * @param data
     */
    void write(byte[] data);

    /**
     * 写入数据包
     * @param data
     */
    void write(DatagramPacket data);

    /**
     * 获取{@link T}对应的原始对象
     * @return {@link T}
     */
    T getSocket();

    /**
     * 设置{@link Executor}，需在 {@link #start()} 之前才有效
     * @param executor
     */
    void setExecutor(Executor executor);

    void setOnSocketStateListener(OnSocketStateListener listener);

    /**
     * 设置消息接收监听器
     * @param listener {@link OnMessageReceivedListener}
     */
    void setOnMessageReceivedListener(OnMessageReceivedListener listener);

    /**
     * 消息接收监听器
     */
    interface OnMessageReceivedListener {
        /**
         * 消息接收
         * @param data
         */
        void onMessageReceived(byte[] data);
    }

    /**
     * 状态监听
     */
    interface OnSocketStateListener {
        /**
         * 启动
         */
        void onStarted();

        /**
         * 关闭
         */
        void onClosed();

        /**
         * 异常
         * @param e
         */
        void onException(Exception e);
    }

}
