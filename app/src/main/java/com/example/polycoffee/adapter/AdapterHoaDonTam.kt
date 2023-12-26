package com.example.polycoffee.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.databinding.ItemHoadonBinding
import com.example.polycoffee.model.HoaDonTemp

class AdapterHoaDonTam(val context: Context, val list:ArrayList<HoaDonTemp>, val maBan:String = "") : RecyclerView.Adapter<AdapterHoaDonTam.ViewHolder>() {
    class ViewHolder(binding:ItemHoadonBinding) : RecyclerView.ViewHolder(binding.root) {
        val tenSP = binding.itemHdTenSP
        val soLuong = binding.itemHdSoLuong
        val view = binding.itemHdRoot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHoadonBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hoaDon = list[position]
        holder.tenSP.text = hoaDon.tenSP
        holder.soLuong.number = hoaDon.soLuong.toString()
        holder.soLuong.setOnValueChangeListener { _, _, newValue ->
            val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("Ban")
            if(newValue==0){
                database.child(maBan).child("ListSP").child(hoaDon.maSP.toString()).removeValue()
            } else{
                database.child(maBan).child("ListSP").child(hoaDon.maSP.toString()).child("soLuong")
                    .setValue(newValue).addOnSuccessListener {
                        Log.d("update soLuong", "Thanh cong")
                    }.addOnFailureListener {
                        Log.d("update soLuong", "That bai")
                    }
            }
        }

    }

    override fun getItemCount(): Int = list.size
}