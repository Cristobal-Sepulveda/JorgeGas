package com.example.conductor.ui.nuevautilidad

import android.Manifest
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.conductor.R
import com.example.conductor.databinding.FragmentNuevaUtilidadBinding
import com.example.conductor.utils.ForegroundOnlyLocationService
import com.example.conductor.utils.SharedPreferenceUtil
import com.example.conductor.utils.toText
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.BuildConfig
import com.example.conductor.utils.Constants.TAG
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class NuevaUtilidadFragment : Fragment(),SharedPreferences.OnSharedPreferenceChangeListener {

    //use Koin to retrieve the ViewModel instance
    private var _binding: FragmentNuevaUtilidadBinding? = null

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    private lateinit var sharedPreferences: SharedPreferences

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaUtilidadBinding.inflate(inflater, container, false)

        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        sharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        _binding!!.buttonForegroundOnlyLocationButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            Log.i("asd","$enabled")
            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }
        return _binding!!.root
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(requireActivity(), ForegroundOnlyLocationService::class.java)
        requireActivity().bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences!!.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.perm_request_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.request_perm_again) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.
                    updateButtonState(false)

                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _binding!!.buttonForegroundOnlyLocationButton.text = getString(R.string.stop_location_updates_button_text)
            _binding!!.buttonForegroundOnlyLocationButton.setBackgroundColor(Color.argb(100, 255, 0, 0))
        } else {
            _binding!!.buttonForegroundOnlyLocationButton.text = getString(R.string.start_location_updates_button_text)
            _binding!!.buttonForegroundOnlyLocationButton.setBackgroundColor(Color.argb(100, 0, 255, 0))
        }
    }

    private fun logResultsToScreen(output: String) {
        val outputWithPreviousLogs = "$output\n${_binding!!.outputTextView.text}"
        _binding!!.outputTextView.text = outputWithPreviousLogs
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                logResultsToScreen("Foreground location: ${location.toText()}, ${LocalDateTime.now()}")
            }
        }
    }


}