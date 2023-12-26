package com.example.polycoffee.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.activity.OrderActivity
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.databinding.DialogHoadonBinding
import com.example.polycoffee.databinding.ItemBanBinding
import com.example.polycoffee.fragments.OrderFragment
import com.example.polycoffee.model.Ban
import com.example.polycoffee.model.HoaDon
import com.example.polycoffee.model.HoaDonTemp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterBan(val context: Context, var list:ArrayList<Ban>, val fragment:OrderFragment, var username:String="",
                 private var role:Int=0) : RecyclerView.Adapter<AdapterBan.ViewHolder>(), Filterable{
    class ViewHolder(binding:ItemBanBinding) : RecyclerView.ViewHolder(binding.root) {
        val view = binding.itemBanView
        val menu = binding.itemBanMenu
        val hoaDon = binding.itemBanHoaDon
        val id = binding.itemBanId
        val state = binding.itemBanState
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBanBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ban = list[position]
        holder.menu.setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra("maBan",ban.maBan)
            context.startActivity(intent)
        }
        holder.state.text = ban.state
        if(ban.state=="Trống"){
            holder.state.setTextColor(Color.BLUE)
        } else{
            holder.state.setTextColor(Color.RED)
        }


        holder.id.text = "Bàn ${list[position].maBan}"

        holder.hoaDon.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val binding = DialogHoadonBinding.inflate(LayoutInflater.from(context))
            builder.setView(binding.root)
                .setTitle("Hóa đơn tạm")
            var alertDialog = builder.create()

            val list = ArrayList<HoaDonTemp>()
            val recyclerView = binding.dialogHoadonRecyclerView

            val adapterHoaDon = AdapterHoaDonTam(context,list,ban.maBan)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapterHoaDon

            val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("Ban")
            database.child(ban.maBan).child("ListSP").addValueEventListener(object : ValueEventListener{
                @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                        list.clear()
                        for(snap in snapshot.children){
                            list.add(snap.getValue(HoaDonTemp::class.java)!!)
                        }
                        val tongTien = list.fold(0){ acc:Int, hoaDonTemp: HoaDonTemp -> acc + hoaDonTemp.donGia * hoaDonTemp.soLuong }
                        binding.dialogHoadonTongTien.text = "Tổng tiền: $tongTien VND"
                        adapterHoaDon.notifyDataSetChanged()
                        if(list.isEmpty()){
                            database.child(ban.maBan).child("state").setValue("Trống")
                            alertDialog.dismiss()
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            database.keepSynced(true)

            database.child(ban.maBan).child("ListSP").get().addOnSuccessListener {
                if(it.value!=null){
                    alertDialog = builder.create()
                    alertDialog.show()
                } else{
                    Toast.makeText(context,"Bàn trống",Toast.LENGTH_SHORT).show()
                }
            }

            if(role == 1){
                builder.setPositiveButton("Đã thanh toán"){ p0, _ ->
                    val databaseHD = FirebaseDatabaseTemp.getDatabase()!!.getReference("HoaDon")
                    databaseHD.get().addOnSuccessListener {
                        val hoaDon = it.children.lastOrNull()?.getValue(HoaDon::class.java)
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("dd-MM-yyyy")
                        if (hoaDon != null){
                            val hoaDonSub = HoaDon(hoaDon.maHD+1,ban.maBan,username,list,sdf.format(cal.time))
                            databaseHD.child(hoaDonSub.maHD.toString()).setValue(hoaDonSub).addOnSuccessListener {
                                Toast.makeText(context,"Thanh cong",Toast.LENGTH_SHORT).show()
                                database.child(ban.maBan).child("ListSP").removeValue()
                                FirebaseDatabase.getInstance().getReference("Ban").child(ban.maBan).child("state").setValue("Trống")
                            } .addOnFailureListener {
                                Toast.makeText(context,"That bai",Toast.LENGTH_SHORT).show()
                            }
                        } else{
                            val hoaDonSub = HoaDon(0,ban.maBan,username,list)
                            databaseHD.child(hoaDonSub.maHD.toString()).setValue(hoaDonSub).addOnSuccessListener {
                                Toast.makeText(context,"Thanh cong",Toast.LENGTH_SHORT).show()
                                database.child(ban.maBan).child("ListSP").removeValue()
                                FirebaseDatabase.getInstance().getReference("Ban").child(ban.maBan).child("state").setValue("Trống")
                            } .addOnFailureListener {
                                Toast.makeText(context,"That bai",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    p0.dismiss()
                }
                    .setNegativeButton("Hủy"){ p0,_ ->
                        p0.dismiss()
                    }

            } else{
                builder.setPositiveButton("Hoàn thành"){
                        p0, _ -> p0.dismiss()
                }
            }

            }
        }


    override fun getItemCount(): Int = list.size
    val oldList = list
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val strSearch =constraint.toString()
                if(strSearch.isEmpty()){
                    list = oldList
                } else{
                    list = oldList.filter { ban -> ban.state.lowercase().contains(strSearch.lowercase()) } as ArrayList<Ban>
                }
                val filterResult = FilterResults()
                filterResult.values = list
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                list = results.values as ArrayList<Ban>
                notifyDataSetChanged()
            }

        }
    }

}