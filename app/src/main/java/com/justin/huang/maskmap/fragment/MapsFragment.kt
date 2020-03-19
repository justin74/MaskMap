package com.justin.huang.maskmap.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.justin.huang.maskmap.R
import com.justin.huang.maskmap.databinding.DrugstoreInfoContentBinding
import com.justin.huang.maskmap.db.Drugstore
import com.justin.huang.maskmap.di.Injectable
import com.justin.huang.maskmap.viewModel.DrugstoreViewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject

class MapsFragment : Fragment(), Injectable, OnMapReadyCallback, InfoWindowAdapter,
    OnInfoWindowClickListener {

    companion object {
        private const val REQUEST_CODE_LOCATION = 123
        private const val DEFAULT_ZOOM = 15f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val drugstoreViewModel: DrugstoreViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var infoWindowBinding: DrugstoreInfoContentBinding
    private var mLastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        infoWindowBinding = DrugstoreInfoContentBinding.inflate(layoutInflater, null, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap ?: return
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
            Timber.d("has permissions")
            getDeviceLocation()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.location_require),
                REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun getDeviceLocation() {
        val locationResult = mFusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                mLastKnownLocation = task.result
                if (mLastKnownLocation != null) {
                    val lat = mLastKnownLocation!!.latitude
                    val lng = mLastKnownLocation!!.longitude
                    Timber.d("mLastKnownLocation: ($lat, $lng)")
                    val latLng = LatLng(lat, lng)

                    //TODO: not start move here?
                    with(mGoogleMap) {
                        isMyLocationEnabled = true
                        setInfoWindowAdapter(this@MapsFragment)
                        setOnInfoWindowClickListener(this@MapsFragment)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                    }
                    subscribeDrugstoresLocation()
                } else {
                    Timber.d("Current location is null. Using defaults.")
                    Timber.e("Exception: $task.exception")
                    mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

    private fun subscribeDrugstoresLocation() {
        drugstoreViewModel.drugStores.observe(this, Observer { drugstores ->
            drugstores?.let {
                //TODO: add worker to get data?
                Timber.d("drugStores count = ${drugstores.size}")
                //TODO: null check?
                addMarkerToMap(drugstores)
            }
        })
    }


    private fun addMarkerToMap(drugstores: List<Drugstore>) {
        //TODO: 只取鄰近地點? bounds? 其他寫法?
        Timber.d("add drugstore marker ")
        drugstores.forEach {
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

    override fun getInfoContents(marker: Marker): View? {
        infoWindowBinding.apply {
            drugstore = marker.tag as Drugstore
            executePendingBindings()
        }
        return infoWindowBinding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(requireContext(), marker.title, Toast.LENGTH_SHORT).show()
    }

//    private fun moveToLocation(latLng: LatLng) {
//        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM).let {
//            mGoogleMap.moveCamera(it)
//        }
//    }

    /**
     * Demonstrates converting a [Drawable] to a [BitmapDescriptor],
     * for use as a marker icon.
     */
//    private fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
//        val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(resources, id, null)
//        if (vectorDrawable == null) {
//            Timber.e("Resource not found")
//            return BitmapDescriptorFactory.defaultMarker()
//        }
//        val bitmap = Bitmap.createBitmap(
//            vectorDrawable.intrinsicWidth,
//            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(bitmap)
//        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
//        vectorDrawable.draw(canvas)
//        return BitmapDescriptorFactory.fromBitmap(bitmap)
//    }
}