package com.example.polycoffee.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.dao.TempFunc
import com.example.polycoffee.databinding.ItemUserBinding
import com.example.polycoffee.fragments.UserFragment
import com.example.polycoffee.model.User

class AdapterUser(var context: Context,var list:ArrayList<User>,var fragment:UserFragment,val accountUsername:String = "") : RecyclerView.Adapter<AdapterUser.ViewHolder>(),Filterable {
    class ViewHolder(binding:ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        val img = binding.itemUserImg
        val username = binding.itemUserUsername
        val password = binding.itemUserPassword
        val hoTen = binding.itemUserHoTen
        val ngaySinh = binding.itemUserNgaySinh
        val diaChi = binding.itemUserDiaChi
        val sdt = binding.itemUserSdt
        val view = binding.root
        val background = binding.background
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.background.background.alpha = 70
        val user = list[position]
        if(user.anhDaiDien!=""){
            holder.img.setImageBitmap(TempFunc.StringToBitmap(user.anhDaiDien))
        }
        holder.username.text = "Username: ${user.userName}"
        holder.password.text = "Passwrod: ${user.passWord}"
        holder.hoTen.text = "Họ tên: ${user.hoTen}"
        holder.ngaySinh.text = "Ngày sinh: ${user.ngaySinh}"
        holder.diaChi.text = "Địa chỉ: ${user.diaChi}"
        holder.sdt.text = "Số điện thoại: ${user.sdt}"

        holder.view.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                TempFunc.choosenDialog(context,user,fragment,"User",accountUsername)
                return false
            }

        })
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
                    list = oldList.filter { user -> user.userName.lowercase().contains(strSearch.lowercase()) } as ArrayList<User>
                }
                val filterResult = FilterResults()
                filterResult.values = list
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                list = results.values as ArrayList<User>
                notifyDataSetChanged()
            }

        }
    }
}