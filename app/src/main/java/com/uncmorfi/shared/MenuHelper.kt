package com.uncmorfi.shared

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

fun Fragment.addMenu(
    @MenuRes menuId : Int,
    onMenuItemSelected : (menuItemId : Int) -> Boolean
){
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuId, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return onMenuItemSelected(menuItem.itemId)
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}