package com.example.polycoffee.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.polycoffee.dao.DAO
import com.example.polycoffee.dao.FirebaseDatabaseTemp
import com.example.polycoffee.dao.TempFunc
import com.example.polycoffee.databinding.ActivityLoginBinding
import com.example.polycoffee.model.User

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var dao: DAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        dao = DAO(this)
        val preferences = getSharedPreferences("USER ACCOUNT", MODE_PRIVATE)
        binding.edLoginUsername.editText!!.setText(preferences.getString("username", ""))
        binding.edLoginPassword.editText!!.setText(preferences.getString("password", ""))
        binding.rdoLoginRMB.isChecked = preferences.getBoolean("check", false)
        binding.btnLogin.setOnClickListener {
            checkLogin()
        }


    }

    fun rememberUser(user: String, password: String, check: Boolean) {
        val preferences = getSharedPreferences("USER ACCOUNT", MODE_PRIVATE)
        val edit = preferences.edit()
        if (!check) {
            edit.clear()
        } else {
            edit.putString("username", user)
            edit.putString("password", password)
            edit.putBoolean("check", check)
        }
        edit.apply()
    }

    private fun checkLogin() {
        val username = binding.edLoginUsername.editText!!.text.toString()
        val password = binding.edLoginPassword.editText!!.text.toString()
        val checkbox = binding.rdoLoginRMB
        TempFunc.checkField(binding.edLoginUsername, binding.edLoginPassword)
        if (TempFunc.noError(binding.edLoginUsername, binding.edLoginPassword)) {
            val database = FirebaseDatabaseTemp.getDatabase()!!.getReference("User")
            val intent = Intent(this, MainActivity::class.java)
            database.get().addOnSuccessListener {
                if (it.child(username).value != null){
                    val user = it.child(username).getValue(User::class.java)
                    val role = user?.role
                    intent.putExtra("role",role)
                    if(user!!.passWord==password){
                        rememberUser(username,password,checkbox.isChecked)
                        Toast.makeText(this@LoginActivity,"Login thành công",Toast.LENGTH_SHORT).show()
                        intent.putExtra("Username",binding.edLoginUsername.editText!!.text.toString())

                        startActivity(intent)
                        finish()
                        database.onDisconnect()
                    } else{
                        Toast.makeText(this@LoginActivity,"Sai mật khẩu",Toast.LENGTH_SHORT).show()
                    }
                } else{
                    Toast.makeText(this@LoginActivity,"Sai tên đăng nhập",Toast.LENGTH_SHORT).show()
                }
            } .addOnFailureListener {
               /* intent.putExtra("role",1)
                startActivity(intent)
                finish()
                database.onDisconnect()*/
               Toast.makeText(this,"Không có kết nối mạng",Toast.LENGTH_SHORT).show()
            }
          /*  database.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "lấy dữ liệu oke", Toast.LENGTH_LONG).show()
                } else {
                    var ex = task.exception;
                    Toast.makeText(this@LoginActivity, "lỗi: ${ex?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }*/

            database.keepSynced(false)
        }
    }
}