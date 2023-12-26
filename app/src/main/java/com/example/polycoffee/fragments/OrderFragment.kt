package com.example.polycoffee.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.adapter.AdapterBan
import com.example.polycoffee.dao.DAO
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.databinding.FragmentOrderBinding
import com.example.polycoffee.model.Ban
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import dev.shreyaspatil.MaterialDialog.MaterialDialog

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    lateinit var adapterBan: AdapterBan
    lateinit var listBan:ArrayList<Ban>
    lateinit var username:String
     var role = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        username = requireActivity().intent.getStringExtra("Username").toString()
        role = requireActivity().intent.getIntExtra("role",0)

        binding.orderFab.setOnClickListener {
            val builder = MaterialDialog.Builder(requireActivity())
                .setTitle("Thêm bàn mới")
                .setMessage("Bạn có chắc thêm bàn mới không")
                .setPositiveButton("Chắc chắn"){
                        p0, _ ->
                    DAO(requireContext()).insert(Ban((listBan.size+1).toString()),"Ban")
                    p0.dismiss()
                }
                .setNegativeButton("Hủy"){ p0, _ -> p0.dismiss()}
                .setCancelable(true)
                .build()
            builder.show()
        }

        binding.showFilterMenu.setOnClickListener {

            val powerMenu = PowerMenu.Builder(requireContext())
                .addItem(PowerMenuItem("Tất cả"))
                .addItem(PowerMenuItem("Trống"))
                .addItem(PowerMenuItem("Chưa thanh toán"))
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setTextGravity(Gravity.CENTER)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(requireContext(), www.sanju.motiontoast.R.color.colorPrimary)).build()
            val onMenuItemClickListener =
                OnMenuItemClickListener<PowerMenuItem> { position, item ->
                    adapterBan.filter.filter(if (position==0)"" else item.title)
                    powerMenu.dismiss()
                }
            powerMenu.onMenuItemClickListener = onMenuItemClickListener
            powerMenu.showAsDropDown(binding.showFilterMenu)
        }

        updateRecyclerView()
        getListLSP()


        return binding.root
    }

    fun updateRecyclerView(){
        listBan = ArrayList()
        recyclerView = binding.orderRecyclerView
        adapterBan = AdapterBan(requireContext(),listBan,this,username,role)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2,RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapterBan
    }

    fun getListLSP(){
        val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("Ban")
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                listBan.clear()
                for (datasnap in snapshot.children){
                    val ban = datasnap.getValue(Ban::class.java)
                    if (ban != null) {
                        listBan.add(ban)
                    }
                }
                adapterBan.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"Failed", Toast.LENGTH_SHORT).show()
            }
        })
        database.keepSynced(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}