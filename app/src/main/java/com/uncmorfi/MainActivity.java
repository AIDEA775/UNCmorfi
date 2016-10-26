package com.uncmorfi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uncmorfi.dialogs.AboutDialog;
import com.uncmorfi.dialogs.BalanceDialog;
import com.uncmorfi.dialogs.SetNameDialog;
import com.uncmorfi.fragments.BalanceFragment;
import com.uncmorfi.fragments.CounterFragment;
import com.uncmorfi.fragments.MapFragment;
import com.uncmorfi.fragments.MenuFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        BalanceDialog.OnNewCardListener, SetNameDialog.OnSetNameListener, OnMapReadyCallback {

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Definir activity_main.xml como layout
        setContentView(R.layout.activity_main);

        // Definir el Toolbar, la barra superior
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Definir la hamburguesa, las 3 lineas horizontales
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Definir el Navigation, el menú deslizante
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Si no venimos de un estado previo, i.e. inicio desde cero
        // meter el FrameLayuot del menú como principal
        if (savedInstanceState == null) {

            // Crear un nuevo Fragment para meter en la activity layout
            Fragment firstFragment = new MenuFragment();

            // Agregar el Fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, firstFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        // Parsear el item
        switch (item.getItemId()) {
            case R.id.nav_menu:
                fragment = new MenuFragment();
                break;
            case R.id.nav_balance:
                fragment = new BalanceFragment();
                break;
            case R.id.nav_counter:
                fragment = new CounterFragment();
                break;
            case R.id.nav_map:
                MapFragment mapFragment = new MapFragment();
                fragment = mapFragment;

                // Registrar escucha onMapReadyCallback
                mapFragment.getMapAsync(this);
                break;
            case R.id.nav_about:
                new AboutDialog().show(getSupportFragmentManager(), "NewUserDialog");
                break;
        }

        if (fragment != null) {
            // Cambiar de Fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            // Actualizar titulo
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(item.getTitle());
        }

        // Cerrar el Navigation
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void newCard(String card) {
        BalanceFragment fragment = (BalanceFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (fragment != null) {
            fragment.newUser(card);
        }
    }

    @Override
    public void setName(String card, String name) {
        BalanceFragment fragment = (BalanceFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (fragment != null) {
            fragment.setName(card, name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng central = new LatLng(-31.439734, -64.189293);
        map.addMarker(new MarkerOptions()
                .position(central)
                .title("Sucursal Central"));

        LatLng belgrano = new LatLng(-31.416686, -64.189000);
        map.addMarker(new MarkerOptions()
                .position(belgrano)
                .title("Sucursal Belgrano"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(central)
                .zoom(16)
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}