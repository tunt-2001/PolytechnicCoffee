package com.example.polycoffee.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.dao.TempFunc
import com.example.polycoffee.databinding.DialogOrderBinding
import com.example.polycoffee.databinding.ItemSpBinding
import com.example.polycoffee.fragments.MenuFragment
import com.example.polycoffee.model.HoaDonTemp
import com.example.polycoffee.model.SanPham

class AdapterSP(val context: Context, var list:ArrayList<SanPham>, val type:Int, var maBan:String="", val fragment: MenuFragment) : RecyclerView.Adapter<AdapterSP.ViewHolder>(),Filterable {
    class ViewHolder(binding:ItemSpBinding) : RecyclerView.ViewHolder(binding.root) {
        val view = binding.itemSpView
        val tenSP = binding.itemSpTenSP
        val gia = binding.itemSpGia
        val img = binding.itemSpImg
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSpBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sanPham = list[position]
        holder.tenSP.text = sanPham.tenSP
        holder.gia.text = "${sanPham.giaSP} VND"
        if(sanPham.img!=""){
            holder.img.setImageBitmap(TempFunc.StringToBitmap(sanPham.img))
        }
        if(type==0){
            holder.view.setOnLongClickListener(object : View.OnLongClickListener{ // giu de mo dialog chon xoa hoac sua
                override fun onLongClick(v: View?): Boolean {
                    TempFunc.choosenDialog(context,sanPham,fragment,"SanPham")
                    return false
                }

            })
        }
        if(type==1){ // order
            holder.view.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                val binding = DialogOrderBinding.inflate(LayoutInflater.from(context))
                builder.setView(binding.root)

                val alertDialog = builder.create()
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alertDialog.show()

                val img = binding.dialogOrderImg
                val tenSP = binding.dialogOrderTenSP
                val soLuongOrder = binding.dialogOrderNumber
                val saveBtn = binding.dialogOrderSaveBtn

                if(sanPham.img!=""){
                    img.setImageBitmap(TempFunc.StringToBitmap(sanPham.img))
                }

                tenSP.text = sanPham.tenSP
                saveBtn.setOnClickListener {
                    if(soLuongOrder.number.toString().toInt() == 0){
                        Toast.makeText(context,"Số lượng phải lớn hơn 0",Toast.LENGTH_SHORT).show()
                    } else{
                        val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("Ban").child(maBan)
                        database.child("ListSP").child(sanPham.maSP.toString()).setValue(HoaDonTemp(sanPham.maSP,sanPham.tenSP,soLuongOrder.number.toString().toInt(),sanPham.giaSP)).addOnSuccessListener {
                            Toast.makeText(context,"Thanh cong",Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                            database.child("state").setValue("Chưa thanh toán")
                        } .addOnFailureListener {
                            Toast.makeText(context,"That bai",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    

    override fun getItemCount(): Int = list.size
    val oldList = list
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val strSearch =constraint.toString()
                if(strSearch.isEmpty()){  // neu khong ghi tu nao trong search view thi
                    list = oldList
                } else{
                    list = oldList.filter { sp -> sp.tenSP.lowercase().contains(strSearch.lowercase()) } as ArrayList<SanPham>
                }
                val filterResult = FilterResults()
                filterResult.values = list
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                list = results.values as ArrayList<SanPham>
                notifyDataSetChanged()
            }

        }
    }
}