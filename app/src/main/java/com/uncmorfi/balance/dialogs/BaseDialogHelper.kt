package com.uncmorfi.balance.dialogs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.uncmorfi.balance.model.User

open class BaseDialogHelper : AppCompatDialogFragment() {
    lateinit var builder: AlertDialog.Builder
    lateinit var user : User

    fun init() {
        user = arguments?.getSerializable(ARG_USER) as User
        builder = AlertDialog.Builder(requireContext())
    }

    fun sendResult(code: Int, user: User) {
        val intent = Intent()
        intent.putExtra(ARG_USER, user)
        targetFragment!!.onActivityResult(targetRequestCode, code, intent)
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        const val ARG_USER : String = "user"

        /*
         * Esta función me llevó toda una noche, y al despertar no recordé como funciona xD
         * La intención era modularizar el código de arriba que se repetía en dos dialogos.
         * Y la idea es devolver una instancia de la clase hija con los parametros asignados.
         */
        fun <T> newInstance(factory: () -> T, fragment: Fragment, code: Int, user: User) : T {
            val args = Bundle()
            args.putSerializable("user", user)

            val dialog = factory() as BaseDialogHelper
            dialog.setTargetFragment(fragment, code)
            dialog.arguments = args
            return dialog as T
        }
    }
}