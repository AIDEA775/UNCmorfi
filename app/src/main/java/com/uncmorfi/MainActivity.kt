package com.uncmorfi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.uncmorfi.about.AboutDialog
import com.uncmorfi.balance.BalanceFragment
import com.uncmorfi.counter.CounterFragment
import com.uncmorfi.faq.FaqFragment
import com.uncmorfi.map.MapFragment
import com.uncmorfi.menu.MenuFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_menu.*

class MainActivity :
        AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

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