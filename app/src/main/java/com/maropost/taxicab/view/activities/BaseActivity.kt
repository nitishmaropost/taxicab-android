package com.maropost.taxicab.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.Button
import com.maropost.taxicab.R
import com.maropost.taxicab.pojomodels.MenuModel
import com.maropost.taxicab.view.adapters.ExpandableListAdapter
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_new.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.list_group_header.view.*


open class BaseActivity : AppCompatActivity() {

    private var expandableListAdapter: ExpandableListAdapter? = null
    private var listDataHeader = ArrayList<MenuModel>()
    private var listDataChild = HashMap<String, ArrayList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        setSupportActionBar(toolbar)
        setSearchListener()
        setupNavigationView()
    }

    /**
     * Navigation settings and data placement
     */
    private fun setupNavigationView() {
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        prepareMenuData()
        populateExpandableList()
        initialiseNavigationItemItemListeners()
        imgToolbarLeftIcon.setOnClickListener{mDrawerLayout.openDrawer(Gravity.START)}
    }

    /**
     * Listen to search events
     */
    private fun setSearchListener() {
        search_view.setVoiceSearch(false); //or false

        /**
         * Detect on text changed for search view
         */
        search_view.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        /**
         * Detect search view opened or closed
         */
        search_view.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
            }

            override fun onSearchViewClosed() {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        val item = menu!!.findItem(R.id.action_search)
        search_view.setMenuItem(item)
        return true
    }

    enum class TransactionType {
        REPLACE, ADD
    }

    /**
     * Set toolbar visibility
     */
    fun showToolbar(bool: Boolean) {
        if (!supportActionBar!!.isShowing) {
            supportActionBar!!.show()
        }
        if(bool)
            toolbarFrame.visibility = View.VISIBLE
        else
            toolbarFrame.visibility = View.GONE
    }

    /**
     * Intercept onback click
     */
    override fun onBackPressed() {
        if (getFragmentCount() == 1 && !search_view.isSearchOpen)
            finish();
        else if(search_view.isSearchOpen)
            search_view.closeSearch()
        else
            super.onBackPressed()
    }

    /**
     * Get backstack fragment count
     */
    private fun getFragmentCount(): Int {
        return supportFragmentManager.backStackEntryCount
    }

    /**
     * Get the current displaying fragment
     */
    fun getCurrentFragment(): Fragment {
        return supportFragmentManager.findFragmentById(R.id.container)!!
    }

    /**
     * Display a snack message
     */
    fun showSnackAlert(message: String) {
        try {
            Snackbar.make(mainContainer, message, Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showPermissionSnackAlert(){
        val snackBar = Snackbar.make(mainContainer, "Access denied!!", Snackbar.LENGTH_SHORT)
                .setAction("t") {
                    val i = Intent()
                    i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    i.addCategory(Intent.CATEGORY_DEFAULT)
                    i.data = Uri.parse("package:" + packageName)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    startActivity(i)
                }
        snackBar.show()
        val snackbarView = snackBar.view
        val action = snackbarView.findViewById(android.support.design.R.id.snackbar_action) as Button
        // action.setBackgroundResource(R.drawable.setting_selector)
        action.setText("")
    }
    /**
     * Replace current fragment with new one
     * addToBackStack - true/false keep it in backstack or not
     */
    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        inflateFragment(fragment, addToBackStack, TransactionType.REPLACE)
    }

    /**
     * Replace current fragment with new one along with Shared Element Transaction
     * addToBackStack - true/false keep it in back stack or not
     */
    fun replaceFragment(fragment: Fragment,addToBackStack: Boolean,view: View) {
        inflateFragment(fragment,addToBackStack, TransactionType.REPLACE,view)
    }

    /**
     * Set your fragment layout in the container
     */
    private fun inflateFragment(fragment:Fragment,addToBackStack:Boolean, transactionType: TransactionType) {
        val transaction = getSupportFragmentManager().beginTransaction();
        val tag = fragment.javaClass.simpleName;
        if (addToBackStack) {
            transaction.addToBackStack(tag)
        } /*else {
            tag = null
        }*/
        when (transactionType) {
            TransactionType.REPLACE -> {
                transaction.replace(R.id.mainContainer, fragment, tag)
            }
            TransactionType.ADD -> {
                transaction.add(R.id.mainContainer, fragment, tag)
            }
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * Set your fragment layout in the container - this method used for transition element animation
     * Could also be used if any other animation needs to be implemented. Apply check based on animation enum type
     */
    private fun inflateFragment(fragment:Fragment, addToBackStack: Boolean, transactionType: TransactionType, view: View?) {
        val transaction = supportFragmentManager.beginTransaction()
        val tag = fragment.javaClass.simpleName
        if(view != null)
            transaction.addSharedElement(view, ViewCompat.getTransitionName(view)!!)
        if(addToBackStack)
            transaction.addToBackStack(tag)
        when (transactionType) {
            TransactionType.REPLACE -> {
                transaction.replace(R.id.mainContainer, fragment, tag)
            }
        }
        transaction.commitAllowingStateLoss()
    }

    /**
     * Pop current displaying fragment
     */
    fun popCurrentFragment() {
        try {
            supportFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Pop all fragments from stack
     */
    fun popAllFragments() {
        val fm = supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            try {
                fm.popBackStack()
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Remove all fragments with tag in backstack
     */
    fun popFragmentByTag(tag:String){
        try {
            supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Set toolbar title
     */
    fun setTitle(title: String) {
        toolbar.setTitle("")
        toolbarTitle.text = title
    }

    /**
     * Set Toolbar left icon
     */
    fun setToolbarLeftIcon(resId: Int){
        imgToolbarLeftIcon.setImageResource(resId)
    }

    /**
     * Set Toolbar right icon
     */
    fun setToolbarRightIcon(resId: Int){
        imgToolbarRightIcon.setImageResource(resId)
    }

    /**
     * Control navigation drawer visibility
     */
    fun showNavigationDrawer(allow:Boolean){
        if(allow)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private fun prepareMenuData() {

        // Adding Group data
        val groupMenuFirst = MenuModel()
        groupMenuFirst.groupName = "Top 250"
        groupMenuFirst.groupIcon = R.drawable.ic_arrow_drop_up
        groupMenuFirst.groupArrowStatus = MenuModel.GroupStatus.COLLAPSED
        listDataHeader.add(groupMenuFirst)

        val groupMenuSecond = MenuModel()
        groupMenuSecond.groupName = "Now Showing"
        groupMenuSecond.groupIcon = R.drawable.ic_arrow_drop_up
        groupMenuSecond.groupArrowStatus = MenuModel.GroupStatus.COLLAPSED
        listDataHeader.add(groupMenuSecond)

        val groupMenuThird = MenuModel()
        groupMenuThird.groupName = "Coming Soon.."
        groupMenuThird.groupIcon = R.drawable.ic_arrow_drop_up
        groupMenuThird.groupArrowStatus = MenuModel.GroupStatus.COLLAPSED
        listDataHeader.add(groupMenuThird)


        // Adding child data
        val group1ChildList = ArrayList<String>()
        group1ChildList.add("The Shawshank Redemption")
        group1ChildList.add("The Godfather")
        group1ChildList.add("The Godfather: Part II")
        group1ChildList.add("Pulp Fiction")
        group1ChildList.add("The Good, the Bad and the Ugly")
        group1ChildList.add("The Dark Knight")
        group1ChildList.add("12 Angry Men")

        val group2ChildList = ArrayList<String>()
        group2ChildList.add("The Conjuring")
        group2ChildList.add("Despicable Me 2")
        group2ChildList.add("Turbo")
        group2ChildList.add("Grown Ups 2")
        group2ChildList.add("Red 2")
        group2ChildList.add("The Wolverine")

        val group3ChildList = ArrayList<String>()
        group3ChildList.add("2 Guns")
        group3ChildList.add("The Smurfs 2")
        group3ChildList.add("The Spectacular Now")
        group3ChildList.add("The Canyons")
        group3ChildList.add("Europa Report")

        listDataChild[listDataHeader[0].groupName] = group1ChildList; // Header, Child data
        listDataChild[listDataHeader[1].groupName] = group2ChildList;
        listDataChild[listDataHeader[2].groupName] = group3ChildList;
    }

    /**
     * Populate list items in Navigation expandable list view
     */
    private fun populateExpandableList() {
        expandableListAdapter = ExpandableListAdapter(this, listDataHeader, listDataChild)
        expandableListView?.setAdapter(expandableListAdapter)
    }

    /**
     * Navigation item group click events
     */
    private fun initialiseNavigationItemItemListeners() {
        // List view Group click listener
        expandableListView.setOnGroupClickListener { parent, view, groupPosition, id ->

            view.imgArrow.setImageResource(R.drawable.ic_arrow_drop_down)

            /*Toast.makeText(this,
                    "Group Clicked " + listDataHeader[groupPosition],
                    Toast.LENGTH_SHORT).show();*/
            false
        }

        // List view Group expanded listener
        expandableListView.setOnGroupExpandListener { groupPosition ->

            val menuModel = listDataHeader[groupPosition]
            menuModel.groupArrowStatus = MenuModel.GroupStatus.EXPANDED
            listDataHeader[groupPosition] = menuModel
            expandableListAdapter?.notifyDataSetChanged()

            /*Toast.makeText(this,
                      listDataHeader[groupPosition].groupName + " Expanded",
                      Toast.LENGTH_SHORT).show()*/
        }

        // List view Group collapsed listener
        expandableListView.setOnGroupCollapseListener { groupPosition ->

            val menuModel = listDataHeader[groupPosition]
            menuModel.groupArrowStatus = MenuModel.GroupStatus.COLLAPSED
            listDataHeader[groupPosition] = menuModel
            expandableListAdapter?.notifyDataSetChanged()

            /*Toast.makeText(this,
                    listDataHeader[groupPosition].groupName + " Collapsed",
                    Toast.LENGTH_SHORT).show()*/
        }

        // List view on child click listener
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            mDrawerLayout.closeDrawer(Gravity.START)
            /*  Toast.makeText(
                      this,
                      listDataHeader[groupPosition].groupName
                              + " : "
                              + listDataChild[listDataHeader[groupPosition].groupName]!![childPosition], Toast.LENGTH_SHORT)
                      .show()*/
            false
        }
    }
}