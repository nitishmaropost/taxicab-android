package com.maropost.taxicab.view.activities

import android.os.Bundle
import com.maropost.taxicab.view.fragments.MapFragment
import com.maropost.taxicab.view.fragments.SplashFragment


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //replaceFragment(SplashFragment(),false)
        replaceFragment(MapFragment(),true)
    }
}
