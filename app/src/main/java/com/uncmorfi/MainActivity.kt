package com.uncmorfi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.uncmorfi.about.AboutDialog
import com.uncmorfi.balance.BalanceFragment
import com.uncmorfi.counter.CounterFragment
import com.uncmorfi.faq.FaqFragment
import com.uncmorfi.map.MapFragment
import com.uncmorfi.menu.MenuFragment

class MainActivity :
        AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private val mDrawerLayout: DrawerLayout by bind(R.id.drawer_layout)
    private val mNavigationView: NavigationView by bind(R.id.nav_view)

    private fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
        return lazy { findViewById<T>(res) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbarAndNavigation()
        if (savedInstanceState == null) {
            setMainFragment()
        }
    }

    private fun setToolbarAndNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close
        )
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mNavigationView.setNavigationItemSelectedListener(this)
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

        mDrawerLayout.closeDrawer(GravityCompat.START)
        return changed
    }

    override fun onBackPressed() {
        when {
            mDrawerLayout.isDrawerOpen(GravityCompat.START) -> {
                mDrawerLayout.closeDrawer(GravityCompat.START)
            }
            mNavigationView.menu.getItem(0).isChecked -> {
                super.onBackPressed()
            }
            else -> {
                // Go to main fragment
                replaceFragment(R.id.nav_menu)
                mNavigationView.setCheckedItem(R.id.nav_menu)
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
            R.id.nav_renovation -> sendRenovationEmail()
            R.id.nav_map -> {
                val mapFragment = MapFragment()
                fragment = mapFragment

                mapFragment.getMapAsync(this)
            }
            R.id.nav_faq -> fragment = FaqFragment()
            R.id.nav_about -> AboutDialog().show(supportFragmentManager, "AboutDialog")
        }
        return fragment
    }

    private fun sendRenovationEmail() {
        val i = Intent(Intent.ACTION_SENDTO)
        i.data = Uri.parse("mailto:")
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("credenciales@comedor.unc.edu.ar"))
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.renovation_email_subject))
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.renovation_email_body))

        if (i.resolveActivity(packageManager) != null)
            startActivity(i)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val fragment = supportFragmentManager.findFragmentById(R.id.content_frame) as MapFragment

        fragment.onMapReady(googleMap)
    }
}