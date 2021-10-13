package com.king.asocket.app.util

import java.lang.Exception
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
object NetworkUtils {

    fun getLocalAddress(): String{
        try{
            return getLocalHostExactAddress()?.hostAddress ?: ""
        }catch (e: Exception){

        }
        return ""
    }

    private fun getLocalHostExactAddress(): InetAddress? {
        try {
            var candidateAddress: InetAddress? = null
            val networkInterfaces: Enumeration<NetworkInterface> =
                NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val iface: NetworkInterface = networkInterfaces.nextElement()
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                val inetAddresses: Enumeration<InetAddress> = iface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress: InetAddress = inetAddresses.nextElement()
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddress.isLoopbackAddress) {
                        if (inetAddress.isSiteLocalAddress) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            return inetAddress
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddress
                        }
                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress ?: InetAddress.getLocalHost()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}