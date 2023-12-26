package com.example.polycoffee.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.R
import com.example.polycoffee.adapter.AdapterSP
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.databinding.FragmentMenuBinding
import com.example.polycoffee.fragments.MenuFragment
import com.example.polycoffee.model.SanPham
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class OrderActivity : AppCompatActivity() {
    lateinit var binding:FragmentMenuBinding
    var listSP = ArrayList<SanPham>()
    lateinit var recyclerView: RecyclerView
    lateinit var adapterSP: AdapterSP
    var maBan = ""
    var type = 1
    var maLoai = "menu1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.rootFragMenu.setBackgroundResource(R.drawable.background10)
        binding.rootFragMenu.background.alpha = 60
        binding.subMenuFab.visibility = View.GONE
        binding.orderSuccessBtn.isVisible = true

        binding.orderSuccessBtn.setOnClickListener {
            onBackPressed()
        }

        binding.menuTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                maLoai =  when(tab.text){
                    "Đồ uống" -> "menu1"
                    "Bánh ngọt" -> "menu2"
                    "Đồ ăn vặt" -> "menu3"
                    else -> "menu1"
                }
                getListLSP()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        maBan = intent.getStringExtra("maBan").toString()
        getListLSP()
        updateRecyclerView()

    }

    fun updateRecyclerView(){
        listSP = ArrayList()
        recyclerView = binding.subMenuRecyclerView
        adapterSP = AdapterSP(this,listSP,type,maBan,MenuFragment())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapterSP
    }

    fun getListLSP(){
        listSP.clear()
        val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("SanPham")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listSP.clear()
                for (datasnap in snapshot.children){
                    val sanPham = datasnap.getValue(SanPham::class.java)
                    if (sanPham != null) {
                        if(sanPham.maLoai==maLoai){
                            listSP.add(sanPham)
                        }
                    }
                }
                listSP.sortWith(compareBy { it.maSP })
                adapterSP.notifyDataSetChanged()
                binding.subMenuProgressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderActivity,"Failed", Toast.LENGTH_SHORT).show()
            }
        })
        database.keepSynced(true)
    }
}