package com.shingetsu.mitadriver.ui.home
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shingetsu.mitadriver.R
import com.shingetsu.mitadriver.Utils.Common
import com.shingetsu.mitadriver.Utils.UserUtils


import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class HomeFragment : Fragment(), OnMapReadyCallback {

    //media player
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mapFragment: SupportMapFragment

    //views
    private lateinit var checkBox: CheckBox
    private lateinit var root_layout: FrameLayout

    private lateinit var googleMap: GoogleMap

    //Location
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //online system
    private lateinit var onlineRef: DatabaseReference
    private var currentUserRef: DatabaseReference? = null
    private lateinit var driverLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire

    //accept flag
    var checkActive: Boolean = false

    private val onlineValueEventListener = object : ValueEventListener {
        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists() && currentUserRef != null)
                currentUserRef!!.onDisconnect().removeValue()

        }

        override fun onCancelled(p0: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), p0.message, Snackbar.LENGTH_LONG).show()
        }

    }

    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        checkBox = view.findViewById(R.id.cb_mode_active)

//        if (checkActive == false) {
//            checkBox.isChecked = false
//        } else {
//            checkBox.isChecked = true
//        }

        //Mode Status of Driver
        checkBox.setOnClickListener {
            if (checkBox.isChecked == true) {
                checkActive = true
                fusedLocationProviderClient!!.lastLocation
                    .addOnFailureListener { e ->
                        Snackbar.make(
                            mapFragment.requireView(),
                            e.message!!,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .addOnSuccessListener { location ->
                        makeDriverOnline(location)
                        UserUtils.updateStatusDriver(requireContext(), checkActive)
                    }
            } else {
                checkActive = false
                if (currentUserRef != null) {
                    currentUserRef!!.removeValue()
                    UserUtils.updateStatusDriver(requireContext(), checkActive)
                }
            }
        }

        initViews(view)
        init()

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    private fun initViews(view: View?) {

        root_layout = view?.findViewById(R.id.root_layout) as FrameLayout

    }

    private fun init() {

        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")


        //registerOnlineSystem()
        //if permission is not allow,dont init it,let user allow it first
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(root_layout, getString(R.string.permission_require), Snackbar.LENGTH_LONG)
                .show()
            return
        }
        buildLocationRequest()
        buildLocationCallback()
        updateLocation()

    }

    private fun updateLocation() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                requireContext()
            )
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Snackbar.make(
//                    root_layout,
//                    getString(R.string.permission_require),
//                    Snackbar.LENGTH_LONG
//                ).show()
//                return
//            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun buildLocationCallback() {
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)

                    val newPos = LatLng(
                        locationResult!!.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
//
//                    if (!isTripStart && valueCheck == true) {
//                        makeDriverOnline(locationResult.lastLocation!!)
//                    } else {
//                        if (!TextUtils.isEmpty(tripNumberId)) {
//                            //update location
//                            val update_data = HashMap<String, Any>()
//                            update_data["currentLat"] = locationResult.lastLocation.latitude
//                            update_data["currentLng"] = locationResult.lastLocation.longitude
//
//                            FirebaseDatabase.getInstance().getReference(Comon.TRIP)
//                                .child(tripNumberId!!)
//                                .updateChildren(update_data)
//                                .addOnFailureListener { e ->
//                                    Snackbar.make(
//                                        mapFragment.requireView(),
//                                        e.message!!,
//                                        Snackbar.LENGTH_LONG
//                                    ).show()
//                                }.addOnSuccessListener { }
//                        }
//                    }

                }
            }
        }
    }

    private fun makeDriverOnline(location: Location) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList: List<Address>?
        try {
            addressList = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            val cityName = addressList[0].adminArea

            driverLocationRef = FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_LOCATION_REFERENCE)
                .child(cityName)
            currentUserRef = driverLocationRef.child(
                FirebaseAuth.getInstance().currentUser!!.uid
            )
            geoFire = GeoFire(driverLocationRef)
            //update location
            geoFire.setLocation(
                FirebaseAuth.getInstance().currentUser!!.uid,
                GeoLocation(
                    location.latitude,
                    location.longitude
                ),

                ) { key: String?, error: DatabaseError? ->
                if (error != null)
                    Snackbar.make(
                        mapFragment.requireView(),
                        error.message,
                        Snackbar.LENGTH_LONG
                    ).show()


            }

            registerOnlineSystem()


        } catch (e: IOException) {
            Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun buildLocationRequest() {
        if (locationRequest == null) {
            locationRequest = LocationRequest()
            locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest!!.fastestInterval = 15000
            locationRequest!!.interval = 10000
            locationRequest!!.smallestDisplacement = 50f
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap!!
        this.googleMap.isMyLocationEnabled = true
        this.googleMap.uiSettings.isMyLocationButtonEnabled = true
        this.googleMap.setOnMyLocationClickListener {

            fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e ->
                    makeText(requireContext(), e.message, LENGTH_SHORT).show()
                }.addOnSuccessListener { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    this.googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            17f
                        )
                    )
                }
            true
        }
        //Layout
        val locationButton = (mapFragment.requireView()
            .findViewById<View>("1".toInt())
            .parent!! as View).findViewById<View>("2".toInt())
        val params = locationButton.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        params.bottomMargin = 50

        //location
        buildLocationRequest()
        buildLocationCallback()
        updateLocation()


    }

    override fun onStop() {
        super.onStop()
        checkActive = false
        UserUtils.updateStatusDriver(requireContext(), checkActive)
        if (currentUserRef != null) {
            currentUserRef!!.removeValue()
        }

    }

    override fun onDestroy() {
        checkActive = false
        UserUtils.updateStatusDriver(requireContext(), checkActive)
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if(checkBox.isChecked == true) {
            checkActive = true
            fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e ->
                    Snackbar.make(
                        mapFragment.requireView(),
                        e.message!!,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .addOnSuccessListener { location ->
                    makeDriverOnline(location)
                    UserUtils.updateStatusDriver(requireContext(), checkActive)
                }
        }
    }

}


