package com.uncmorfi.menu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import com.uncmorfi.R
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.MemoryHelper
import com.uncmorfi.helpers.RecyclerTouchListener
import com.uncmorfi.helpers.SnackbarHelper.SnackType
import com.uncmorfi.helpers.SnackbarHelper.showSnack
import java.io.File
import java.util.*

/**
 * Menú de la semana.
 * Administra la UI y el guardado persistente del menú.
 * Usa a [RefreshMenuTask] para actualizar el mra la UI y el guardado persistente del menú.
 * Usa a [RefreshMenuTask] para actualizar el menú.
 */
class MenuFragment : Fragment(), RefreshMenuTask.RefreshMenuListener {
    private lateinit var mRootView: View
    private lateinit var mMenuAdapter: MenuAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mApplicationContext: Context

    private val menuLastModified: Long
        get() {
            val menuFile = File(requireActivity().filesDir.toString() + "/" + MENU_FILE)
            return menuFile.lastModified()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fragment_menu, container, false)

        mSwipeRefreshLayout = mRootView.findViewById(R.id.menu_swipe_refresh)
        mApplicationContext = requireActivity().applicationContext

        initSwipeRefreshLayout()
        initRecyclerAndAdapter()
        initMenu()

        if (needAutoRefreshMenu())
            refreshMenu()

        return mRootView
    }

    private fun initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener { refreshMenu() }

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(
                R.color.accent)
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.white,
                R.color.primary_light
        )
    }

    private fun initRecyclerAndAdapter() {
        mRecyclerView = mRootView.findViewById(R.id.menu_list)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(
                DividerItemDecoration(mApplicationContext, DividerItemDecoration.VERTICAL))

        mRecyclerView.addOnItemTouchListener(RecyclerTouchListener(mApplicationContext,
                mRecyclerView, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {}

            override fun onLongClick(view: View, position: Int) {
                val food = mMenuAdapter.menuList[position].food
                val result = TextUtils.join(", ", food) + "\n\n#UNCmorfi"
                val clipboard = mApplicationContext.
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Food", result)
                clipboard.primaryClip = clip
                showSnack(context, mRootView, R.string.menu_copy_msg, SnackType.FINISH)
            }
        }))

        val layoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = layoutManager
    }

    private fun initMenu() {
        val menuSaved = MemoryHelper.readStringFromStorage(context, MENU_FILE)
        val menuList: List<DayMenu>

        menuList = if (menuSaved != null)
            DayMenu.fromJson(menuSaved)
        else
            ArrayList()

        mMenuAdapter = MenuAdapter(mApplicationContext, menuList)
        mRecyclerView.adapter = mMenuAdapter
    }

    private fun needAutoRefreshMenu(): Boolean {
        val now = Calendar.getInstance()
        now.time = Date()
        val nowWeek = now.get(Calendar.WEEK_OF_YEAR)
        val nowYear = now.get(Calendar.YEAR)

        val menu = Calendar.getInstance()
        menu.timeInMillis = menuLastModified
        val menuWeek = menu.get(Calendar.WEEK_OF_YEAR)
        val menuYear = now.get(Calendar.YEAR)

        return menuYear < nowYear || menuWeek < nowWeek
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_menu)
    }

    override fun onStop() {
        super.onStop()
        mSwipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_update) {
            refreshMenu()
            return true
        } else if (item.itemId == R.id.menu_browser) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshMenu() {
        if (ConnectionHelper.isOnline(mApplicationContext)) {
            mSwipeRefreshLayout.isRefreshing = true
            RefreshMenuTask(mApplicationContext, this).execute()
        } else {
            mSwipeRefreshLayout.isRefreshing = false
            showSnack(context, mRootView, R.string.no_connection, SnackType.ERROR)
        }
    }

    override fun onRefreshMenuSuccess(menu: List<DayMenu>) {
        if (activity != null && isAdded) {
            mSwipeRefreshLayout.isRefreshing = false
            mMenuAdapter.updateMenu(menu)
            showSnack(context, mRootView, R.string.update_success, SnackType.FINISH)
        }
    }

    override fun onRefreshMenuFail() {
        if (activity != null && isAdded)
            mSwipeRefreshLayout.isRefreshing = false
        showSnack(context, mRootView, R.string.update_fail, SnackType.ERROR)
    }

    companion object {
        private const val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"
        @JvmStatic val MENU_FILE = "menu"
    }

}