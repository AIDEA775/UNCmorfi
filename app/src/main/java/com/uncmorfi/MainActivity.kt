package com.uncmorfi

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.uncmorfi.about.AboutDialog
import com.uncmorfi.balance.BalanceFragment
import com.uncmorfi.counter.CounterFragment
import com.uncmorfi.faq.FaqFragment
import com.uncmorfi.map.MapFragment
import com.uncmorfi.menu.MenuFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.widget.ViewDragHelper
import com.uncmorfi.helpers.sendEmail
import com.uncmorfi.helpers.openFacebook

class MainActivity :
        AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbarAndNavigation()
        if (savedInstanceState == null) {
            setMainFragment()
        }
    }

    private fun setToolbarAndNavigation() {
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        // Abrir drawerLayout desde mas o menos la mitad de la pantalla
        // https://stackoverflow.com/a/17802569
        val mDragger = drawerLayout.javaClass.getDeclaredField("mLeftDragger")
        mDragger.isAccessible = true
        val draggerObj = mDragger.get(drawerLayout) as ViewDragHelper

        val mEdgeSize = draggerObj.javaClass.getDeclaredField("mEdgeSize")
        mEdgeSize.isAccessible = true
        val edge = mEdgeSize.getInt(draggerObj)

        mEdgeSize.setInt(draggerObj, edge * 3) // any constant in dp
    }

    private fun setMainFragment() {
        val firstFragment = MenuFragment()
        supportFragmentManager
                .beginTransaction()
                .add(R.id.content_frame, firstFragment)
                .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var changed = true

        if (!item.isChecked)
            changed = replaceFragment(item.itemId)

        drawerLayout.closeDrawer(GravityCompat.START)
        return changed
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            navView.menu.getItem(0).isChecked -> {
                super.onBackPressed()
            }
            else -> {
                // Go to main fragment
                replaceFragment(R.id.nav_menu)
                navView.setCheckedItem(R.id.nav_menu)
            }
        }
    }

    private fun replaceFragment(item: Int): Boolean {
        val fragment = getFragmentById(item)

        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit()
        }
        return fragment != null
    }

    private fun getFragmentById(id: Int): Fragment? {
        var fragment: Fragment? = null

        when (id) {
            R.id.nav_menu -> fragment = MenuFragment()
            R.id.nav_balance -> fragment = BalanceFragment()
            R.id.nav_counter -> fragment = CounterFragment()
            R.id.nav_renovation -> sendEmail("credenciales@estudiantiles.unc.edu.ar",
                    R.string.renovation_email_subject,
                    R.string.renovation_email_body)
            R.id.nav_map -> fragment = MapFragment()
            R.id.nav_faq -> fragment = FaqFragment()
            R.id.nav_face -> openFacebook("https://web.facebook.com/UNCmorfi/")
            R.id.nav_about -> AboutDialog().show(supportFragmentManager, "AboutDialog")
        }
        return fragment
    }

}