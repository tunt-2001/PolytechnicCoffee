package com.example.polycoffee.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.adapter.AdapterSP
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.dao.TempFunc
import com.example.polycoffee.databinding.DialogSanphamBinding
import com.example.polycoffee.databinding.FragmentMenuBinding
import com.example.polycoffee.model.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class MenuFragment : Fragment() {

    var bitmapSP:Bitmap? = null
    var listSP = ArrayList<SanPham>()
    lateinit var img:ImageView
    lateinit var recyclerView: RecyclerView
    lateinit var adapterSP: AdapterSP
    var type = 0
    var maBan = ""
    var maLoai = "menu1"
    lateinit var progressBar: ProgressBar

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        binding.subMenuFab.setOnClickListener {
            openDialogSP(SanPham(),0,requireContext())
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

        binding.searchViewMenu.setOnQueryTextListener(object : SearchView.OnQueryTextListener,  //searchView query
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapterSP.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapterSP.filter.filter(newText)
                return false
            }
        })

        binding.menuTab.getTabAt(1)!!.select()
        binding.menuTab.getTabAt(0)!!.select()
        updateRecyclerView()
        return binding.root
    }

    fun updateRecyclerView(){
        progressBar = binding.subMenuProgressBar
        listSP = ArrayList()
        recyclerView = binding.subMenuRecyclerView
        adapterSP = AdapterSP(requireContext(),listSP,type,maBan,this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapterSP
    }

    fun getListLSP(){ //read data SP
        listSP.clear()
        val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("SanPham")
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
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
                progressBar.visibility = View.GONE
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

    fun openDialogSP(sanPham: SanPham, type:Int, context: Context){
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        val binding = DialogSanphamBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        bitmapSP = null

        val maSP = binding.dialogSpMaSP
        img = binding.dialogSpImg
        val tenSP = binding.dialogSpTenSP
        val gia = binding.dialogSpGia

        maSP.editText!!.isEnabled = false

        if(type==1){
            maSP.editText!!.setText(sanPham.maSP.toString())
            tenSP.editText!!.setText(sanPham.tenSP)
            gia.editText!!.setText(sanPham.giaSP.toString())
            if(sanPham.img!=""){
                img.setImageBitmap(TempFunc.StringToBitmap(sanPham.img))
                bitmapSP = TempFunc.StringToBitmap(sanPham.img)
            }
        }

        img.setOnClickListener {
            CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1,1).start(requireContext(),this)
        }
        if(type==0){
            val database = FirebaseDatabase.getInstance().getReference("SanPham")
            database.get().addOnSuccessListener {
                val sp = it.children.lastOrNull()?.getValue(SanPham::class.java)
                if(sp!=null){
                    maSP.editText!!.setText("${sp.maSP+1}")
                } else{
                    maSP.editText!!.setText("${0}")
                }
            }
        }
        binding.dialogSpSaveBtn.setOnClickListener {
            TempFunc.checkField(tenSP,gia)
            if(!"^[0-9]+$".toRegex().matches(gia.editText!!.text.toString())){
                gia.error = "Giá tiền phải là số"
            } else gia.error = null
            if(TempFunc.noError(tenSP,gia)){
                val sanPhamSub = SanPham(maSP.editText!!.text.toString().toInt(),tenSP.editText!!.text.toString(),gia.editText!!.text.toString().toInt(),if(type==0) maLoai else sanPham.maLoai,if(bitmapSP==null) "" else  TempFunc.BitMapToString(bitmapSP!!))
                FirebaseDatabaseTemp.getDatabase()!!.getReference("SanPham").child(maSP.editText!!.text.toString()).setValue(sanPhamSub)
                    .addOnFailureListener { Toast.makeText(context,"That bai",Toast.LENGTH_SHORT).show() }
                    .addOnSuccessListener { Toast.makeText(context,"Thanh cong",Toast.LENGTH_SHORT).show()}
                alertDialog.dismiss()
                updateRecyclerView()
            }
        }

        binding.dialogSpCancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,result.uri)
                bitmapSP = bitmap
                img.setImageBitmap(bitmapSP)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val error = result.error
                Log.d("error",error.toString())
            }
        }
    }
}