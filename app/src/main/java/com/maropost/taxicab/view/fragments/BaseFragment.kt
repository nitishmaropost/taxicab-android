package com.maropost.taxicab.view.fragments

import android.support.v4.app.Fragment
import android.view.View
import com.maropost.taxicab.view.activities.BaseActivity
import kotlinx.android.synthetic.main.app_bar_main.*


open class BaseFragment : Fragment() {

    fun replaceFragment(fragment: BaseFragment, isAdd: Boolean) {
        try {
            (activity as BaseActivity).replaceFragment(fragment, isAdd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Replace current fragment with new one along with Shared Element Transaction
     * addToBackStack - true/false keep it in back stack or not
     */
    fun replaceFragment(fragment: Fragment,addToBackStack: Boolean,view: View) {
        (activity as BaseActivity).replaceFragment(fragment,addToBackStack,view)
    }

    fun showSnackAlert(message: String?) {
        if (activity as BaseActivity? == null || message == null) {
            return
        }
        (activity as BaseActivity).showSnackAlert(message)
    }

    fun showPermissionSnackAlert(){
        (activity as BaseActivity).showPermissionSnackAlert()
    }

    /**
     * Pop current displaying fragment
     */
    fun popCurrentFragment() {
        try {
            (activity as BaseActivity).popCurrentFragment();
        } catch (e:Exception) {
          e.printStackTrace()
        }
    }

    fun popFragmentByTag(tag:String){
        try {
            (activity as BaseActivity).popFragmentByTag(tag);
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Set toolbar title
     */
    fun setTitle(title: String) {
        (activity as BaseActivity).toolbar.title = ""
        (activity as BaseActivity).toolbarTitle.text = title
    }

    /**
     * Control navigation drawer visibility
     */
    fun showNavigationDrawer(allow:Boolean){
        (activity as BaseActivity).showNavigationDrawer(allow)
    }

    /**
     * Set toolbar visibility
     */
    fun showToolbar(bool: Boolean) {
        (activity as BaseActivity).showToolbar(bool)
    }

}