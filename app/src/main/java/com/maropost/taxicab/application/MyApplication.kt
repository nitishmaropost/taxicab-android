package com.maropost.taxicab.application

import android.app.Application

class MyApplication : Application() {


    init {
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null

        fun getInstance() : MyApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        if(instance == null)
            instance = this
    }
}