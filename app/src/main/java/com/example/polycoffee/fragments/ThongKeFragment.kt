package com.example.polycoffee.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polycoffee.adapter.AdapterThongKe
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.databinding.FragmentThongKeBinding
import com.example.polycoffee.model.HoaDon
import com.example.polycoffee.model.HoaDonTemp
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ThongKeFragment : Fragment() {

    private var _binding: FragmentThongKeBinding? = null
    private val binding get() = _binding!!
    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("dd-MM-yyyy")
    lateinit var list : ArrayList<HoaDon>
    lateinit var adapterThongKe: AdapterThongKe
    lateinit var recyclerView: RecyclerView
    val cal: Calendar = Calendar.getInstance()

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentThongKeBinding.inflate(inflater, container, false)

        val tuNgay = binding.thongkeTuNgay
        val denNgay = binding.thongkeDenNgay
        val calBtn = binding.thongkeResultBtn
        recyclerView = binding.thongkeRecyclerView
        tuNgay.editText!!.setText(sdf.format(cal.time))
        denNgay.editText!!.setText(sdf.format(cal.time))
        chooseDate(tuNgay,denNgay)
        updateRecyclerView()
        calBtn.setOnClickListener {
            val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("HoaDon")
            database.get().addOnSuccessListener {
                list.clear()
                for(snap in it.children){
                    val hoaDon = snap.getValue(HoaDon::class.java)
                    if (hoaDon != null) {
                        if (parseDate(hoaDon.ngay)!! >= parseDate(tuNgay.editText!!.text.toString()) && parseDate(hoaDon.ngay)!! <= parseDate(denNgay.editText!!.text.toString())){
                            list.add(hoaDon)
                        }
                    }
                    adapterThongKe.notifyDataSetChanged()
                }
                list.sortBy { hoaDon ->
                    hoaDon.ngay
                }
                var sum = 0
                list.forEach { hoaDon ->
                    sum += hoaDon.listSP.fold(0) { acc: Int, hoaDonTemp: HoaDonTemp ->
                        acc + hoaDonTemp.donGia * hoaDonTemp.soLuong
                    }
                }
                binding.thongkeTongLoiNhuan.text = "Tổng lợi nhuận: $sum VND"
            }
            database.keepSynced(true)
        }
        calBtn.performClick()


        return binding.root
    }

    fun updateRecyclerView(){
        list = ArrayList()
        adapterThongKe = AdapterThongKe(requireContext(),list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapterThongKe
    }

    @SuppressLint("ClickableViewAccessibility")
    fun chooseDate(vararg textInputLayout: TextInputLayout){
        for (textinput in textInputLayout){
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, i, i2, i3 ->
                cal.set(Calendar.YEAR,i)
                cal.set(Calendar.MONTH,i2)
                cal.set(Calendar.DAY_OF_MONTH,i3)
                textinput.editText!!.setText(sdf.format(cal.time))
            }
            textinput.editText!!.setOnTouchListener(@SuppressLint("ClickableViewAccessibility")
            object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_UP){
                        val picker = DatePickerDialog(requireContext(),dateSetListener,cal.get(Calendar.YEAR),cal.get(
                            Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))
                            picker.show()
                        return true
                    }
                    return true
                }

            })
        }
    }

    fun parseDate(date:String): Date? {
        return sdf.parse(date)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}