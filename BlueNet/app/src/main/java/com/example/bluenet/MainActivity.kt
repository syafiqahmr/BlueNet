package com.example.bluenet

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bluenet.databinding.ActivityMainBinding
import com.example.bluenet.ui.register.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.altbeacon.beacon.*
import org.altbeacon.beacon.powersave.BackgroundPowerSaver


class MainActivity : AppCompatActivity(), BeaconConsumer {
    private lateinit var binding: ActivityMainBinding
    private var beaconManager: BeaconManager? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null
    private var crowd = -1
    private val user = FirebaseAuth.getInstance().currentUser!!.uid.substring(0, 16)
    private val ref = Firebase.database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var PERMISSION_REQUEST_FINE_LOCATION = 1
        var PERMISSION_REQUEST_BACKGROUND_LOCATION = 2

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("This app needs background location access")
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                PERMISSION_REQUEST_BACKGROUND_LOCATION
                            )
                        }
                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { }
                        builder.show()
                    }
                }
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        PERMISSION_REQUEST_FINE_LOCATION
                    )
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        beaconManager!!.bind(this)

        val beacon = Beacon.Builder()
            .setId1(asciiToHex(user))
            .setId2("1")
            .setId3("2")
            .setManufacturer(0x0118)
            .setTxPower(-59)
            .setDataFields(listOf(1.toLong()))
            .build()
        val beaconParser = BeaconParser()
            .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")
        val beaconTransmitter = BeaconTransmitter(applicationContext, beaconParser)
        beaconTransmitter.startAdvertising(beacon)

        backgroundPowerSaver = BackgroundPowerSaver(this)

        ref.child("users").child(user).get().addOnSuccessListener {
            if (it.getValue(User::class.java)?.type == "Booth") {
                crowd = 0
            }
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_traffic,
                R.id.navigation_namecards,
                R.id.navigation_my_namecard,
                R.id.navigation_scan_namecard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBeaconServiceConnect() {
        beaconManager!!.removeAllRangeNotifiers()
        beaconManager!!.addRangeNotifier(RangeNotifier { beacons, region ->
            if (beacons.isNotEmpty()) {
                if (crowd > -1 && crowd != beacons.size) {
                    val tfref = FirebaseDatabase.getInstance().getReference("traffic")
                    tfref.child(user).setValue(beacons.size)
                }
                val beaconsIterator = beacons.iterator()
                runOnUiThread {
                    while (beaconsIterator.hasNext()) {
                        val newUser = hexToASCII(beaconsIterator.next().id1.toString())
                        val ncref = FirebaseDatabase.getInstance().getReference("listOfNamecards")
                        ncref.child(user).child(newUser!!).setValue("")
                    }
                }
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


    private fun asciiToHex(asciiValue: String): String? {
        val chars = asciiValue.toCharArray()
        val hex = StringBuffer()
        for (i in chars.indices) {
            hex.append(Integer.toHexString(chars[i].toInt()))
        }
        return hex.toString()
    }

    private fun hexToASCII(hexValue: String): String? {
        var newHexValue = hexValue.replace("-", "")
        val output = StringBuilder("")
        var i = 0
        while (i < newHexValue.length) {
            val str = newHexValue.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }


}