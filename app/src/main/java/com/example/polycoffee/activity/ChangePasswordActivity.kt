package com.example.polycoffee.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.dao.TempFunc.Companion.checkField
import com.example.polycoffee.dao.TempFunc.Companion.noError
import com.example.polycoffee.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding:ActivityChangePasswordBinding
    lateinit var password:String
    lateinit var username:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rootChangePass.background.alpha = 60
        val newPass = binding.dmkNewPass
        password = intent.getStringExtra("password").toString()
        username = intent.getStringExtra("username").toString()

        binding.dmkSaveBtn.setOnClickListener {
            if(valiDateForm()){
                FirebaseDatabaseTemp.getDatabase()!!.getReference("User").child(username).child("passWord").setValue(newPass.editText!!.text.toString()).addOnSuccessListener {
                    Toast.makeText(this@ChangePasswordActivity,"Cập nhật mật khẩu thành công",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        binding.dmkResetBtn.setOnClickListener {
            onBackPressed()
            this@ChangePasswordActivity.finish()
        }
    }

    private fun valiDateForm():Boolean{
        checkField(binding.dmkOldPass,binding.dmkNewPass,binding.dmkNewPassRepeat)
        if(noError(binding.dmkOldPass,binding.dmkNewPass,binding.dmkNewPassRepeat)){
            val newPass = binding.dmkNewPass.editText!!.text.toString()
            val rePass = binding.dmkNewPassRepeat.editText!!.text.toString()
            val oldPass = binding.dmkOldPass.editText!!.text.toString()
            if(password != oldPass){
                binding.dmkOldPass.error = "Mật khẩu cũ sai"
                return false
            } else {
                binding.dmkOldPass.error = null
                if(binding.dmkNewPass.editText!!.text.toString().length<6 || binding.dmkNewPass.editText!!.text.toString().length>30){
                    binding.dmkNewPass.error = "Mật khẩu phải từ 6 đến 30 ký tự"
                    return false
                } else {
                    binding.dmkNewPass.error = null
                    if(newPass != rePass){
                        binding.dmkNewPassRepeat.error = "Mật khẩu không trùng khớp"
                        return false
                    } else binding.dmkNewPassRepeat.error = null
                }
            }

            return true
        }
        return false
    }
}