package com.uncmorfi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.uncmorfi.databinding.ActivityMainBinding
import com.uncmorfi.ui.about.AboutDialog
import com.uncmorfi.ui.balance.BalanceFragment
import com.uncmorfi.ui.faq.FaqFragment
import com.uncmorfi.ui.home.HomeFragment
import com.uncmorfi.ui.map.MapFragment
import com.uncmorfi.ui.menu.MenuFragment
import com.uncmorfi.ui.reminders.RemindersFragment
import com.uncmorfi.ui.servings.ServingsFragment
import com.uncmorfi.shared.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this,R.string.notify_permissions,Toast.LENGTH_SHORT).show()
        } else {
            // Do nothing
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.setUi()

        if (savedInstanceState == null) {
            setMainFragment()
        }

        viewModel.refreshWorkers()

        observe(viewModel.state) { state ->
            Log.i("MainActivity", "new status: $state")
            binding.contentLayout.snack(state)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    binding.navView.menu.getItem(0).isChecked -> {
                        finish()
                    }
                    else -> {
                        // Go to main fragment
                        replaceFragment(R.id.nav_home)
                        binding.navView.setCheckedItem(R.id.nav_home)
                    }
                }

            }
        })
    }

    private fun ActivityMainBinding.setUi(){
        setContentView(root)
        setToolbarAndNavigation()
    }

    private fun setToolbarAndNavigation() {
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_open,
            R.string.navigation_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
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

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return changed
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
            R.id.nav_reminders -> return RemindersFragment()
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
        binding.navView.setCheckedItem(id)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Do nothing
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this,R.string.notify_permissions_rationale,Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}