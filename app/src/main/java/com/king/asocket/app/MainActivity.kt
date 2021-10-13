package com.king.asocket.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.king.asocket.app.tcp.TCPClientActivity
import com.king.asocket.app.tcp.TCPServerActivity
import com.king.asocket.app.udp.UDPClientActivity
import com.king.asocket.app.udp.UDPMulticastActivity
import com.king.asocket.app.udp.UDPServerActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }



    fun onClick(v: View){
        when(v.id){
            R.id.btnTCPClient -> startActivity(Intent(this, TCPClientActivity::class.java))
            R.id.btnTCPServer -> startActivity(Intent(this, TCPServerActivity::class.java))
            R.id.btnUDPClient -> startActivity(Intent(this, UDPClientActivity::class.java))
            R.id.btnUDPServer -> startActivity(Intent(this, UDPServerActivity::class.java))
            R.id.btnUDPMulicast -> startActivity(Intent(this, UDPMulticastActivity::class.java))
        }
    }
}