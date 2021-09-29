package com.king.asocket;


import com.king.asocket.tcp.TCPClient;
import com.king.asocket.tcp.TCPServer;
import com.king.asocket.udp.UDPMulticast;
import com.king.asocket.udp.UDPClient;
import com.king.asocket.udp.UDPServer;
import com.king.asocket.util.LogUtils;

import org.junit.Test;


import java.net.DatagramPacket;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String HOST = "192.168.1.101";
//    private static final String HOST = "192.168.1.255";
    private static final String GROUP_HOST = "224.1.1.88";
    private static final int GROUP_PORT = 8888;


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void testTCPClient(){
        //测试时需不使用Logcat，因为Logcat相关属于Android才有的
        LogUtils.setShowLog(false);
        final TCPClient client = new TCPClient(HOST,9007);
        client.setOnMessageReceivedListener(new ISocket.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(byte[] value) {
                System.out.println("TCPClient:" + new String(value));
                byte[] data = "TCPClient".getBytes();
                client.write(data);
            }
        });
        client.start();

    }

    @Test
    public void testTCPServer(){
        //测试时需不使用Logcat，因为Logcat相关属于Android才有的
        LogUtils.setShowLog(false);
        final TCPServer server = new TCPServer(9007);
        server.setOnMessageReceivedListener(new ISocket.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(byte[] value) {
                System.out.println("TCPServer:" + new String(value));
                byte[] data = "TCPServer".getBytes();
                server.write(data);
            }
        });
        server.start();
    }

    @Test
    public void testUDPMulticast(){
        //测试时需不使用Logcat，因为Logcat相关属于Android才有的
        LogUtils.setShowLog(false);
        final UDPMulticast client = new UDPMulticast(GROUP_HOST,GROUP_PORT);
        client.setOnMessageReceivedListener(new ISocket.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(byte[] value) {
                System.out.println("UDPMulticast:" + new String(value));
                byte[] data = "UDPMulticast".getBytes();
                client.write(data);
            }
        });
        client.start();

    }

    @Test
    public void testUDPClient(){
        //测试时需不使用Logcat，因为Logcat相关属于Android才有的
        LogUtils.setShowLog(false);
        final UDPClient client = new UDPClient(HOST,9008,9010);
        client.setOnMessageReceivedListener(new ISocket.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(byte[] value) {
                System.out.println("UDPClient:" + new String(value));
                byte[] data = "UDPClient".getBytes();
                client.write(data);
            }
        });
        client.start();
        System.out.println(String.format("localAddress:%s:%d",client.getSocket().getLocalAddress().getHostAddress(),client.getSocket().getLocalPort()));
    }

    @Test
    public void testUDPServer(){
        //测试时需不使用Logcat，因为Logcat相关属于Android才有的
        LogUtils.setShowLog(false);
        final UDPServer server = new UDPServer(9010);
        server.setOnMessageReceivedListener(new ISocket.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(byte[] value) {
                System.out.println("UDPServer:" + new String(value));
                try{
                    byte[] data = "UDPServer".getBytes();
                    DatagramPacket packet = new DatagramPacket(data,0,data.length, InetAddress.getByName(HOST),9009);
                    server.write(packet);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        server.start();
    }

}