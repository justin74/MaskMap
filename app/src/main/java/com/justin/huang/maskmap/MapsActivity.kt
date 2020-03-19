package com.justin.huang.maskmap

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.justin.huang.maskmap.databinding.DrugstoreInfoContentBinding
import com.justin.huang.maskmap.db.DrugStore
import com.justin.huang.maskmap.viewModel.DrugStoreViewModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_maps.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject

class MapsActivity : AppCompatActivity(), OnInfoWindowClickListener, OnMapReadyCallback,
    HasAndroidInjector {

    companion object {
        private const val REQUEST_CODE_LOCATION = 123
        private const val DEFAULT_ZOOM = 15f
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val drugStoreViewModel: DrugStoreViewModel by viewModels {
        viewModelFactory
    }

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLastKnownLocation: Location? = null

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        private val binding = DrugstoreInfoContentBinding.inflate(layoutInflater, null, false)

        override fun getInfoContents(marker: Marker): View? {
            binding.apply {
                drugstore = marker.tag as DrugStore
                executePendingBindings()
            }
            return binding.root
        }

        override fun getInfoWindow(marker: Marker): View? {
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: 轉向時處理 location, use viewModel?
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
        (map as SupportMapFragment).getMapAsync(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        enableMyLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * enableMyLocation() will enable the location of the map if the user has given permission
     * for the app to access their device location.
     * Android Studio requires an explicit check before setting map.isMyLocationEnabled to true
     * but we are using EasyPermissions to handle it so we can suppress the "MissingPermission"
     * check.
     */
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION)
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            getDeviceLocation()
            subscribeDrugStoresLocation()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.location_require),
                REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getDeviceLocation() {
        val locationResult = mFusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                mLastKnownLocation = task.result
                if (mLastKnownLocation != null) {
                    val lat = mLastKnownLocation!!.latitude
                    val lng = mLastKnownLocation!!.longitude
                    Timber.d("current location: ($lat, $lng)")
                    val latLng = LatLng(lat, lng)
                    //TODO: not start move here?
                    with(mGoogleMap) {
                        isMyLocationEnabled = true
                        setInfoWindowAdapter(CustomInfoWindowAdapter())
                        setOnInfoWindowClickListener(this@MapsActivity)
                    }
                    moveToLocation(latLng)
                } else {
                    Timber.d("Current location is null. Using defaults.")
                    Timber.e("Exception: $task.exception")
                    moveToLocation(mDefaultLocation)
                    mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

    private fun moveToLocation(latLng: LatLng) {
        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM).let {
            mGoogleMap.moveCamera(it)
        }
    }

    private fun subscribeDrugStoresLocation() {
        drugStoreViewModel.drugStores.observe(this, Observer { drugStores ->
            drugStores?.let {
                //TODO: add worker to get data?
                Timber.d("drugStores count = ${drugStores.size}")
                //TODO: null check?
                addMarkerToMap(drugStores)
            }
        })
    }

    private fun addMarkerToMap(drugStores: List<DrugStore>) {
        //TODO: 只取鄰近地點? bounds?
        Timber.d("add drugstore marker ")
        drugStores.forEach {
            mGoogleMap.addMarker(
                MarkerOptions().apply {
                    position(LatLng(it.latitude, it.longitude))
                    title(it.name)
                    icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(it.maskAdult)))
                    snippet(it.id)
                    //TODO: zIndex?
                }
            ).tag = it
        }
    }

    private fun getMarkerIcon(adultMaskAmount: Int): Int {
        //TODO: 圖片多層問題?
        return when (adultMaskAmount) {
            0 -> R.drawable.mask_empty
            in 1 until 20 -> R.drawable.mask_few
            in 20 until 200 -> R.drawable.mask_less
            else -> R.drawable.mask_many
        }
    }

    /**
     * Demonstrates converting a [Drawable] to a [BitmapDescriptor],
     * for use as a marker icon.
     */
    private fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Timber.e("Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onInfoWindowClick(marker: Marker) {
        //TODO: go to detail?
        Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
    }
}
