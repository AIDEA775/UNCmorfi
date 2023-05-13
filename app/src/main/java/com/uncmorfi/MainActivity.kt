package com.uncmorfi

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.uncmorfi.about.AboutDialog
import com.uncmorfi.balance.BalanceFragment
import com.uncmorfi.faq.FaqFragment
import com.uncmorfi.home.HomeFragment
import com.uncmorfi.map.MapFragment
import com.uncmorfi.menu.MenuFragment
import com.uncmorfi.reservations.ReservationFragment
import com.uncmorfi.servings.ServingsFragment
import com.uncmorfi.shared.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbarAndNavigation()
        if (savedInstanceState == null) {
            setMainFragment()
        }

        observe(viewModel.status) {
            Log.i("MainActivity", "new status: $it")
            content_layout.snack(it)
        }

        observe(viewModel.isLoading) {
            val refreshLayout = content_layout.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            refreshLayout?.isRefreshing = it
        }

        observe(viewModel.reservation) {
            content_layout.snack(it)
        }

        observe(viewModel.reserveTry) {
            if (it > 0) {
                content_layout.snack(getString(R.string.snack_loop).format(it), SnackType.LOADING)
            }
        }
    }

    private fun setToolbarAndNavigation() {
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_open,
            R.string.navigation_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    private fun setMainFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.content_frame, HomeFragment())
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
                replaceFragment(R.id.nav_home)
                navView.setCheckedItem(R.id.nav_home)
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
        when (id) {
            R.id.nav_home -> return HomeFragment()
            R.id.nav_menu -> return MenuFragment()
            R.id.nav_balance -> return BalanceFragment()
            R.id.nav_servings -> return ServingsFragment()
            R.id.nav_reservation -> return ReservationFragment()
            R.id.nav_renovation -> sendEmail(
                "credenciales@estudiantiles.unc.edu.ar",
                R.string.renovation_email_subject,
                R.string.renovation_email_body
            )
            R.id.nav_map -> return MapFragment()
            R.id.nav_faq -> return FaqFragment()
            R.id.nav_face -> openFacebook()
            R.id.nav_github -> startBrowser("https://github.com/AIDEA775/UNCmorfi")
            R.id.nav_about -> AboutDialog().show(supportFragmentManager, "AboutDialog")
        }
        return null
    }

    fun change(id: Int) {
        replaceFragment(id)
        navView.setCheckedItem(id)
    }
}