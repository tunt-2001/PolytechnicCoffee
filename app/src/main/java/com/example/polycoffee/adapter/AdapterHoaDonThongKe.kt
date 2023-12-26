package com.example.polycoffee.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.databinding.ItemListsptronghdBinding
import com.example.polycoffee.model.HoaDonTemp

class AdapterHoaDonThongKe(val context: Context, val list:ArrayList<HoaDonTemp>) : RecyclerView.Adapter<AdapterHoaDonThongKe.ViewHolder>() {
    class ViewHolder(binding:ItemListsptronghdBinding) : RecyclerView.ViewHolder(binding.root) {
        val tenSP = binding.itemListspTensP
        val soLuong = binding.itemListspSoLuong
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListsptronghdBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hoaDonTemp = list[position]
        holder.tenSP.text = hoaDonTemp.tenSP
        holder.soLuong.text = hoaDonTemp.soLuong.toString()
    }

    override fun getItemCount(): Int = list.size
}