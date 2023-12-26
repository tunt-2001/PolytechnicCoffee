package com.example.polycoffee.model

data class HoaDon(
    val maHD:Int=0, val maBan: String ="", val userName:String="",
    var listSP:ArrayList<HoaDonTemp> = ArrayList(),
    val ngay:String="")
