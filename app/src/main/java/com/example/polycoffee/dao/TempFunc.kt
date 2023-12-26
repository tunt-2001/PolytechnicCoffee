package com.example.polycoffee.dao

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.polycoffee.fragments.MenuFragment
import com.example.polycoffee.fragments.UserFragment
import com.example.polycoffee.model.SanPham
import com.example.polycoffee.model.User
import com.google.android.material.textfield.TextInputLayout
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import java.io.ByteArrayOutputStream

class TempFunc {
    companion object{
        fun BitMapToString(bitmap: Bitmap):String{
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b,Base64.DEFAULT)
        }
        fun StringToBitmap(imgStr:String):Bitmap{
            val imgBytes = Base64.decode(imgStr,0)
            return BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.size)
        }
        fun choosenDialog(context:Context,objectAny: Any,fragmentAny: Any,refName:String,username:String = ""){
            val builder = MaterialDialog.Builder(context as Activity)
                    .setTitle("Chọn chức năng")
                    .setNegativeButton("Xóa"){ p0, p1 ->     // Set nút xóa cho dialog
                        p0.dismiss()
                        if(objectAny is User && objectAny.userName == username){
                            Toast.makeText(context,"Không thể xóa",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.d("sa","tjsaoj")
                            val builderRemove = MaterialDialog.Builder(context)
                                .setTitle("Xóa")
                                .setMessage("Bạn chắc chắn xóa chứ?")
                                .setNegativeButton("Cancel"
                                ) { px, _ ->
                                    px.dismiss()
                                }.setPositiveButton("Chắc chắn"){
                                        px, _ ->
                                    DAO(context).remove(objectAny,refName)   // xóa từ DAO
                                    when(fragmentAny){
                                        is MenuFragment -> fragmentAny.updateRecyclerView() //Cập nhật lại recyclerview
                                        is UserFragment -> fragmentAny.updateRecyclerView()
                                        //is OrderActivity -> fragmentAny.updateRecyclerView()
                                    }
                                    px.dismiss()
                                }.build()
                            builderRemove.show()
                        }
                    }
                    .setPositiveButton("Sửa"){   // set nút sửa cho dialog
                        p0,p1 ->
                        p0.dismiss()
                        when(fragmentAny){
                            is MenuFragment ->  fragmentAny.openDialogSP(objectAny as SanPham,1,context) //mở dialog sửa của các fragment
                            is UserFragment -> fragmentAny.openDialog(objectAny as User,1)
                        }
                    }.setCancelable(true).build()
            builder.show()
        }

        fun checkField(vararg check: TextInputLayout){
            for (view in check){
                if(view.editText!!.text.isEmpty()){
                    view.error = "Bạn phải nhập vào trường này"
                } else view.error = null
            }
        }
        fun noError(vararg check: TextInputLayout):Boolean{
            for (view in check){
                if (view.error!=null) return false
            }
            return true
        }
    }
}