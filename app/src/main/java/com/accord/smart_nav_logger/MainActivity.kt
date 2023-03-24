package com.accord.smart_nav_logger

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.accord.smart_nav_logger.App.Companion.prefs
import com.accord.smart_nav_logger.PreferenceUtils.isTrackingStarted
import com.accord.smart_nav_logger.data.LoggingRepository
import com.accord.smart_nav_logger.data.SharedHamsaMessageManager
import com.accord.smart_nav_logger.ui.main.SectionsPagerAdapter
import com.accord.smart_nav_logger.databinding.ActivityMainBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var  loggingRepository:LoggingRepository

    private var isServiceBound = false
    private var service: MainService? = null
    private var switch: SwitchMaterial? = null

    private var locationFlow: Job? = null


    private var foregroundOnlyServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as MainService.LocalBinder
            service = binder.service
            isServiceBound = true
            if (locationFlow?.isActive== true) {
                // Activity started location updates but service wasn't bound yet - tell service to start now
                service?.subscribeToLocationUpdates()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            service = null
            isServiceBound = false
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        observeLocationFlow()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val serviceIntent = Intent(this, MainService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        val item = menu?.findItem(R.id.gps_switch_item)
        if (item != null) {
            switch = MenuItemCompat.getActionView(item).findViewById(R.id.gps_switch)

            // Initialize state of GPS switch before we set the listener, so we don't double-trigger start or stop
            switch!!.isChecked = isTrackingStarted(prefs)

            // Set up listener for GPS on/off switch
            switch!!.setOnClickListener {
                // Turn GPS on or off
                if (!switch!!.isChecked && isTrackingStarted(prefs)) {
                    service?.unsubscribeToLocationUpdates()
                } else {
                    if (switch!!.isChecked && !isTrackingStarted(prefs)) {
                        service?.subscribeToLocationUpdates()
                    }
                }
            }


        }


            return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        when (item.itemId) {
            R.id.gps_switch -> {
                return true
            }
            R.id.share -> {
              //  share()
                return true
            }
            R.id.filter_sats -> {
               // UIUtils.showFilterDialog(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @ExperimentalCoroutinesApi
    private fun observeLocationFlow() {
        // This should be a Flow and not LiveData to ensure that the Flow is active before the Service is bound
        if (locationFlow?.isActive == true) {
            // If we're already observing updates, don't register again
            return
        }
        // Observe locations via Flow as they are generated by the repository
        locationFlow = loggingRepository.getLocation()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
              //  lastLocation = it
                //Log.d(TAG, "Activity location: ${it.toNotificationTitle()}")

              //  hideProgressBar()

                // Reset the options menu to trigger updates to action bar menu items
                invalidateOptionsMenu()

              //  benchmarkController?.onLocationChanged(it)
            }
            .launchIn(lifecycleScope)
    }

}