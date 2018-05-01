package com.uncmorfi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.uncmorfi.about.AboutDialog;
import com.uncmorfi.balance.BalanceFragment;
import com.uncmorfi.counter.CounterFragment;
import com.uncmorfi.help.HelpFragment;
import com.uncmorfi.map.MapFragment;
import com.uncmorfi.menu.MenuFragment;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbarAndNavigation();
        if (savedInstanceState == null) {
            setMainFragment();
        }
    }

    private void setToolbarAndNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void setMainFragment() {
        Fragment firstFragment = new MenuFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_frame, firstFragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean changed = true;

        if (!item.isChecked())
            changed = replaceFragment(item.getItemId());

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return changed;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mNavigationView.getMenu().getItem(0).isChecked()) {
            super.onBackPressed();
        } else {
            // Go to main fragment
            replaceFragment(R.id.nav_menu);
            mNavigationView.setCheckedItem(R.id.nav_menu);
        }
    }

    private boolean replaceFragment(int item) {
        Fragment fragment = getFragmentById(item);

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
        return fragment != null;
    }

    private Fragment getFragmentById(int id) {
        Fragment fragment = null;

        switch (id) {
            case R.id.nav_menu:
                fragment = new MenuFragment();
                break;
            case R.id.nav_balance:
                fragment = new BalanceFragment();
                break;
            case R.id.nav_counter:
                fragment = new CounterFragment();
                break;
            case R.id.nav_renovation:
                sendRenovationEmail();
                break;
            case R.id.nav_map:
                MapFragment mapFragment = new MapFragment();
                fragment = mapFragment;

                mapFragment.getMapAsync(this);
                break;
            case R.id.nav_help:
                fragment = new HelpFragment();
                break;
            case R.id.nav_about:
                new AboutDialog().show(getSupportFragmentManager(), "AboutDialog");
                break;
        }
        return fragment;
    }

    private void sendRenovationEmail() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"credenciales@comedor.unc.edu.ar"});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.renovation_email_subject));
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.renovation_email_body));

        if (i.resolveActivity(getPackageManager()) != null)
            startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapFragment fragment = (MapFragment)
                getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (fragment != null)
            fragment.onMapReady(googleMap);
    }
}