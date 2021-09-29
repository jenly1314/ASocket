package com.king.asocket.app.tcp

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.king.asocket.ASocket
import com.king.asocket.app.R
import com.king.asocket.app.databinding.ActivityTcpClientBinding
import com.king.asocket.udp.UDPClient
import com.king.asocket.util.LogUtils
import java.net.DatagramSocket

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
class TCPClientActivity() : AppCompatActivity() {

    val binding by lazy {
        ActivityTcpClientBinding.inflate(layoutInflater)
    }

    var mHost = "192.168.1.101"

    var mPort = 9007

    var aSocket: ASocket<DatagramSocket>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.etHost.setText(mHost)
        binding.etPort.setText(mPort.toString())

    }

    override fun onDestroy() {
        aSocket?.close()
        super.onDestroy()
    }

    private fun clickStart(){
//        if(aSocket?.isStart == true){
//            System.out.println("start")
//        }else{
        mHost = binding.etHost.text.toString()
        mPort = binding.etPort.text.toString().toInt()
        val client = UDPClient(mHost,mPort)
        aSocket = ASocket(client)
        aSocket?.start()
        LogUtils.d("start...")
//        }
    }

    private fun clickSend(){
        if(!TextUtils.isEmpty(binding.etContent.text)){
            aSocket?.let {
                it.write(binding.etContent.text.toString().toByteArray())
                binding.etContent.setText("")
            }
        }
    }

    private fun clickClear(){
        binding.tvContent.text = ""
    }

    fun onClick(v: View){
        when(v.id){
            R.id.btnStart -> clickStart()
            R.id.btnSend -> clickSend()
            R.id.btnClear -> clickClear()

        }
    }
}