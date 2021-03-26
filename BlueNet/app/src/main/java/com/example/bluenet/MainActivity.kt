package com.example.bluenet

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bluenet.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.altbeacon.beacon.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var beaconManager: BeaconManager? = null

    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))

        user = FirebaseAuth.getInstance().currentUser!!

//        val beacon = Beacon.Builder()
//                .setId1(user.uid)
//                .setId2("1")
//                .setId3("2")
//                .setManufacturer(0x0118)
//                .setTxPower(-59)
//                .build()
//        val beaconParser = BeaconParser()
//                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")
//        val beaconTransmitter = BeaconTransmitter(applicationContext, beaconParser)
//        beaconTransmitter.startAdvertising(beacon)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_traffic, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun onBeaconServiceConnect() {
        beaconManager!!.removeAllRangeNotifiers()
        beaconManager!!.addRangeNotifier(RangeNotifier { beacons, region ->
            if (beacons.isNotEmpty()) {
                Log.i(
                    "Beacon",
                    "There are " + beacons.size + " beacons detected."
                )
            }
        })
        try {
            beaconManager!!.startRangingBeaconsInRegion(
                Region(
                    "myRangingUniqueId",
                    null,
                    null,
                    null
                )
            )
        } catch (e: RemoteException) {
        }
    }
}