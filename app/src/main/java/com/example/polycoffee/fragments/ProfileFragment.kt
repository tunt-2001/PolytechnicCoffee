package com.example.polycoffee.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.polycoffee.activity.ChangePasswordActivity
import com.example.polycoffee.activity.LoginActivity
import com.example.polycoffee.adapter.AdapterUser
import com.example.polycoffee.dao.DAO
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.dao.TempFunc
import com.example.polycoffee.databinding.DialogProfileBinding
import com.example.polycoffee.databinding.FragmentProfileBinding
import com.example.polycoffee.model.User
import com.theartofdev.edmodo.cropper.CropImage
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: AdapterUser
    lateinit var img: ImageView
    var bitmapTemp:Bitmap? = null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        var user = User()
        val username = binding.tvProfileUsername
        val role = binding.tvRole
        val hoTen = binding.tvProfileHoten
        val diaChi = binding.tvProfileDiachi
        val ngaySinh = binding.tvProfileNgaysinh
        val sdt = binding.tvProfileSdt
        val imgProfile = binding.imgProfile

        val AccUsername = requireActivity().intent.getStringExtra("Username").toString()
        val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("User").child(AccUsername)
        database.get().addOnSuccessListener {
            user = it.getValue(User::class.java)!!
            username.text = user.userName
            role.text = if(user.role==0)"Nhân viên" else "Quản lý"
            hoTen.text = "Họ tên: ${user.hoTen}"
            diaChi.text = "Địa chỉ: ${user.diaChi}"
            ngaySinh.text = "Ngày sinh: ${user.ngaySinh}"
            sdt.text = "Số điện thoại: ${user.sdt}"
            if(user.anhDaiDien!=""){
                imgProfile.setImageBitmap(TempFunc.StringToBitmap(user.anhDaiDien))
            }
        }.addOnFailureListener { Toast.makeText(requireContext(),"failed",Toast.LENGTH_SHORT).show() }

        database.keepSynced(true)



        binding.btnProfileDMK.setOnClickListener {
            val intent = Intent(requireActivity(), ChangePasswordActivity::class.java)
            intent.putExtra("password",user.passWord)
            intent.putExtra("username",AccUsername)
            startActivity(intent)
        }
        binding.btnProfileEdit.setOnClickListener {
            openDialog(user)
        }
        binding.btnProifleLogout.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return root
    }
    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility", "SetTextI18n")
    fun openDialog(user: User){
        val builder = AlertDialog.Builder(requireContext())
        val bindingD = DialogProfileBinding.inflate(layoutInflater)
        builder.setView(bindingD.root)
        val alertDialog = builder.create()
        alertDialog.show()

        img = bindingD.dialogProfileImg
        val username = bindingD.dialogProfileUsername
        val hoten = bindingD.dialogProfileHoTen
        val ngaySinh = bindingD.dialogProfileNgaySinh
        val diaChi = bindingD.dialogProfileDiaChi
        val sdt = bindingD.dialogProfileSDT
        val saveBtn = bindingD.dialogProfileBtnSave
        val cancelBtn = bindingD.dialogProfileBtnCancel

            username.editText!!.setText(user.userName)
            username.editText!!.isEnabled = false
            hoten.editText!!.setText(user.hoTen)
            ngaySinh.editText!!.setText(user.ngaySinh)
            diaChi.editText!!.setText(user.diaChi)
            sdt.editText!!.setText(user.sdt)
            if(user.anhDaiDien!=""){
                img.setImageBitmap(TempFunc.StringToBitmap(user.anhDaiDien))
                bitmapTemp = TempFunc.StringToBitmap(user.anhDaiDien)
            }

        img.setOnClickListener {
            CropImage.activity().setAspectRatio(1,1).start(requireContext(),this)
        }
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, i, i2, i3 ->
            cal.set(Calendar.YEAR,i)
            cal.set(Calendar.MONTH,i2)
            cal.set(Calendar.DAY_OF_MONTH,i3)
            ngaySinh.editText!!.setText(sdf.format(cal.time))
        }
        ngaySinh.editText!!.setOnTouchListener(object : View.OnTouchListener{
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

        saveBtn.setOnClickListener {
            TempFunc.checkField(username,hoten,ngaySinh,diaChi,sdt)
            val regexPhone = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}\$".toRegex()
            if(!regexPhone.matches(sdt.editText!!.text.toString())){
                sdt.error = "Sai định dạng số điện thoại"
            } else{
                sdt.error = null
            }

            if(TempFunc.noError(username,hoten,ngaySinh,diaChi,sdt)){
                user.hoTen = hoten.editText!!.text.toString()
                user.ngaySinh = ngaySinh.editText!!.text.toString()
                user.diaChi = diaChi.editText!!.text.toString()
                user.sdt = sdt.editText!!.text.toString()
                user.anhDaiDien = if(bitmapTemp == null)"" else TempFunc.BitMapToString(bitmapTemp!!)
                DAO(requireContext()).insert(user,"User")
                if(user.anhDaiDien!=""){
                    binding.imgProfile.setImageBitmap(TempFunc.StringToBitmap(user.anhDaiDien))
                }
                binding.tvProfileHoten.text = "Họ tên: ${user.hoTen}"
                binding.tvProfileDiachi.text = "Địa chỉ: ${user.diaChi}"
                binding.tvProfileNgaysinh.text = "Ngày sinh: ${user.ngaySinh}"
                binding.tvProfileSdt.text = "Số điện thoại: ${user.sdt}"
                alertDialog.dismiss()
            }

        }
        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == Activity.RESULT_OK){
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,result.uri)
                bitmapTemp = bitmap
                img.setImageBitmap(bitmapTemp)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val error = result.error
                Log.d("error",error.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}