package com.example.polycoffee.dao

import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseTemp {
    companion object{
        private var mDatabase: FirebaseDatabase? = null
        fun getDatabase(): FirebaseDatabase? {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance()
                mDatabase!!.setPersistenceEnabled(true)
            }
            return mDatabase
        }
    }
}