package com.example.conductor.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.ACTION_MAP_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.EXTRA_LOCATION
import com.example.conductor.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class LocationService : Service() {

    private val dataSource: AppDataSource by inject()

    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false
    private var serviceRunningInForeground = false
    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var vistaGeneralFragmentLocationCallback: LocationCallback
    private lateinit var mapFragmentLocationCallback: LocationCallback
    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null




    override fun onCreate() {
        Log.d("LocationService", "onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // FusedLocationProviderClient - Main class for receiving location updates.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // LocationRequest - Requirements for the location updates, i.e.,
        // how often you should receive
        // updates, the priority, etc.
        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(6)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(5)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.SECONDS.toMillis(8)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        /** Antena VistaGeneralFragment : LocationCallback
         * Called from VistaGeneralFragment when FusedLocationProviderClient has a
         * new Location. */
        vistaGeneralFragmentLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)


                /* Normally, you want to save a new location to a database. We are simplifying
                   things a bit and just saving it as a local variable, as we only need it again
                   if a Notification is created (when the user navigates away from app).*/
                currentLocation = locationResult.lastLocation
                /* Notify our Activity that a new location was added. Again, if this was a
                   production app, the Activity would be listening for changes to a database
                   with new locations, but we are simplifying things a bit to focus on just
                   learning the location side of things. */
                val intent = Intent(ACTION_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
        /** Antena MapFragment : LocationCallback
         * Called from MapFragment when FusedLocationProviderClient has a
         * new Location. */
        mapFragmentLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                    /* Normally, you want to save a new location to a database. We are simplifying
                        things a bit and just saving it as a local variable, as we only need it again
                        if a Notification is created (when the user navigates away from app).
                        */


                currentLocation = locationResult.lastLocation
                /* Notify our Activity that a new location was added. Again, if this was a
                   production app, the Activity would be listening for changes to a database
                   with new locations, but we are simplifying things a bit to focus on just
                   learning the location side of things. */
                val intent = Intent(ACTION_MAP_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }



    }

    //APIS de comunicacion con la App
    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdatesVistaGeneralFragment() {
        Log.d("LocationService", "subscribeToLocationUpdates()")
        try{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, vistaGeneralFragmentLocationCallback, Looper.getMainLooper())
            startForegroundService(Intent(applicationContext, LocationService::class.java))
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }catch(e:Exception){
            Log.d("LocationService", "subscribeToLocationUpdates() error: ${e.message}")
        }
    }



    fun unsubscribeToLocationUpdatesVistaGeneralFragment(){
        Log.d("LocationService", "unsubscribeToLocationUpdates()")
        try {
            fusedLocationProviderClient.removeLocationUpdates(vistaGeneralFragmentLocationCallback)
            stopSelf()
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            Log.d("LocationService", "unsubscribeToLocationUpdates() error: ${unlikely.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdatesMapFragment() {
        Log.d("LocationService", "subscribeToLocationUpdatesMapFragment()")
        try{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, mapFragmentLocationCallback, Looper.getMainLooper())
            startForegroundService(Intent(applicationContext, LocationService::class.java))
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }catch(e:Exception){
            Log.d("LocationService", "subscribeToLocationUpdatesMapFragment() error: ${e.message}")
        }
    }

    fun unsubscribeToLocationUpdatesMapFragment(){
        Log.d("LocationService", "unsubscribeToLocationUpdatesMapFragment()")
        try {
            fusedLocationProviderClient.removeLocationUpdates(mapFragmentLocationCallback)
            stopSelf()
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            Log.d("LocationService", "unsubscribeToLocationUpdatesMapFragment() error: ${unlikely.message}")
        }
    }


    private fun generateNotification(mainText: Int): Notification {
        val mainNotificationText = getString(mainText)
        val titleText = getString(R.string.app_name)
        val notificationCompatBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)

        /*val activityPendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)*/

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.icono_app_foreground)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //.setContentIntent(activityPendingIntent)
            .build()
    }

    //Called by the system every time a client explicitly starts the service by calling startForegroundService(Intent),
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand()")
        startForeground(1, generateNotification(R.string.servicio_rastreo_iniciado))
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LocationService", "onBind()")
        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d("LocationService", "onRebind()")
        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("LocationService", "onUnbind()")
        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d("LocationService", "Start foreground service")
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

}
