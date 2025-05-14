# ASocket

[![MavenCentral](https://img.shields.io/maven-central/v/com.github.jenly1314/asocket?logo=sonatype)](https://repo1.maven.org/maven2/com/github/jenly1314/ASocket)
[![JitPack](https://img.shields.io/jitpack/v/github/jenly1314/ASocket?logo=jitpack)](https://jitpack.io/#jenly1314/ASocket)
[![CI](https://img.shields.io/github/actions/workflow/status/jenly1314/ASocket/build.yml?logo=github)](https://github.com/jenly1314/ASocket/actions/workflows/build.yml)
[![Download](https://img.shields.io/badge/download-APK-brightgreen?logo=github)](https://raw.githubusercontent.com/jenly1314/ASocket/master/app/release/app-release.apk)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen?logo=android)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![License](https://img.shields.io/github/license/jenly1314/ASocket?logo=open-source-initiative)](https://opensource.org/licenses/mit)

ASocket 是一个TCP/UDP协议的封装库，方便快速实现TCP的长连接与UDP的单播、组播、广播等相关通信。

> 通过 ASocket 统一管理 TCP/UDP 相关 Socket，让其适用于Android，在UI主线程调用和回调，在子线程异步处理消息的发送与接收

## 效果展示
![Image](GIF.gif)

> 你也可以直接下载 [演示App](https://raw.githubusercontent.com/jenly1314/ASocket/master/app/release/app-release.apk) 体验效果

## 引入

### Gradle:

1. 在Project的 **build.gradle** 或 **setting.gradle** 中添加远程仓库

    ```gradle
    repositories {
        //...
        mavenCentral()
    }
    ```

2. 在Module的 **build.gradle** 中添加依赖项
    ```gradle
    implementation 'com.github.jenly1314:asocket:1.0.0'
    
    ```

## 使用

### 特别说明

#### 组播IP地址特别说明
>  多播的地址是特定的，D类地址用于多播。D类IP地址就是多播IP地址，即224.0.0.0至239.255.255.255之间的IP地址，并被划分为局部连接多播地址、预留多播地址和管理权限多播地址3类：
>  局部多播地址：在224.0.0.0～224.0.0.255之间，这是为路由协议和其他用途保留的地址，路由器并不转发属于此范围的IP包。
>  预留多播地址：在224.0.1.0～238.255.255.255之间，可用于全球范围（如Internet）或网络协议。
>  管理权限多播地址：在239.0.0.0～239.255.255.255之间，可供组织内部使用，类似于私有IP地址，不能用于Internet，可限制多播范围。

### 代码示例
```kotlin
    //初始化一个ISocket的实现类（如：TCPClient、TCPServer、UDPClient、UDPServer、UDPMulticast）
    val tcpClient = TCPClient(host,port)
    //初始化ASocket
    val aSocket = ASocket(tcpClient)
    //设置状态监听
    aSocket.setOnSocketStateListener(object : ISocket.OnSocketStateListener{
        override fun onStarted() {

        }
    
        override fun onClosed() {
    
        }
    
        override fun onException(e: Exception) {

        }
    
    })
    //设置接收消息监听
    aSocket.setOnMessageReceivedListener { data ->
        //TODO 接收消息
    }
    //启动
    aSocket.start()


    //....
    //发送消息
    aSocket.write(data)

```

### 完整示例

- TCPClient示例：[TCPClientActivity](app/src/main/java/com/king/asocket/app/tcp/TCPClientActivity.kt)

- TCPServer示例：[TCPServerActivity](app/src/main/java/com/king/asocket/app/tcp/TCPServerActivity.kt)

- UDPClient示例：[UDPClientActivity](app/src/main/java/com/king/asocket/app/udp/UDPClientActivity.kt)

- UDPServer示例：[UDPServerActivity](app/src/main/java/com/king/asocket/app/udp/UDPServerActivity.kt)

- UDPMulticast示例：[UDPMulticastActivity](app/src/main/java/com/king/asocket/app/udp/UDPMulticastActivity.kt)

更多使用详情，请查看[Demo](app)中的源码使用示例或直接查看[API帮助文档](https://jitpack.io/com/github/jenly1314/ASocket/latest/javadoc/)

## 相关推荐

- [ANetty](https://github.com/jenly1314/ANetty) 基于Netty封装的Android链路通讯库，用以快速开发高性能，高可靠性的网络交互。在保证易于开发的同时还保证其应用的性能，稳定性和伸缩性。
- [AWebSocket](https://github.com/jenly1314/AWebSocket) 基于okhttp封装的 WebSocket，简洁易用。

## 版本日志

#### v1.0.0：2021-10-13
*  ASocket初始版本

---

![footer](https://jenly1314.github.io/page/footer.svg)

