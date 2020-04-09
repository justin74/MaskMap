package com.justin.huang.maskmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.justin.huang.maskmap.databinding.ActivityMapsBinding
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    OnMapClickListener, ClusterManager.OnClusterItemClickListener<DrugStore>, HasAndroidInjector {

    companion object {
        private const val REQUEST_CODE_LOCATION = 123
        private const val START_ZOOM = 6f
        private const val CURRENT_LOCATION_ZOOM = 15f
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val drugStoreViewModel: DrugStoreViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLastKnownLocation: Location? = null
    private lateinit var binding: ActivityMapsBinding
    private lateinit var drugstore: DrugStore
    private lateinit var mClusterManager: ClusterManager<DrugStore>
    private val metrics = DisplayMetrics()

    // Default location
    private var latlng = LatLng(23.973875, 120.982024)

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: 轉向時處理 location, use viewModel?
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_maps)
        windowManager.defaultDisplay.getMetrics(metrics)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        binding.apply {
            fabLocation.setOnClickListener {
                Timber.e("location FAB click")
                enableMyLocation()
            }

            fabRefresh.setOnClickListener {
                Timber.e("refresh FAB click")
                drugStoreViewModel.fetchMaskPoints()
            }

            chipPhoneCallback = object : ChipCallback {
                override fun onChipClick(view: View, drugstore: DrugStore?) {
                    drugstore?.let {
                        when (view.id) {
                            R.id.chip_navigation -> {
                                val gmmIntentUri =
                                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${drugstore.latitude},${drugstore.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                startActivity(mapIntent)
                            }
                            R.id.chip_phone -> {
                                val intent =
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${drugstore.phone}"))
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
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
    override fun onMapReady(googleMap: GoogleMap?) {
        Timber.e("onMapReady")
        mGoogleMap = googleMap ?: return
        with(mGoogleMap) {
            mClusterManager = ClusterManager(this@MapsActivity, this)
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            //setOnMarkerClickListener(this@MapsActivity)
            setOnMapClickListener(this@MapsActivity)
            setOnMarkerClickListener(mClusterManager)
            setOnCameraIdleListener(mClusterManager)
            animateToLocation(latlng, START_ZOOM)
        }
        with(mClusterManager) {
            renderer = DrugstoreRender()
            algorithm = NonHierarchicalViewBasedAlgorithm(metrics.widthPixels, metrics.heightPixels)
            setOnClusterItemClickListener(this@MapsActivity)
        }
        enableMyLocation()
        subscribeDrugStoresLocation()
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
            mGoogleMap.isMyLocationEnabled = true // this setup need permission
            getDeviceLocation()
        } else {
            mGoogleMap.isMyLocationEnabled = false
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
//                    val latLng =
//                        LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                    latlng = LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                    Timber.d("current location: $latlng")
                    animateToLocation(latlng, CURRENT_LOCATION_ZOOM)
                } else {
                    Timber.d("Current location is null. Using defaults.")
                    Timber.e("Exception: $task.exception")
                }
            }
        }
    }

    private fun moveToLocation(latLng: LatLng) {
        CameraUpdateFactory.newLatLngZoom(latLng, CURRENT_LOCATION_ZOOM).let {
            mGoogleMap.moveCamera(it)
        }
    }

    private fun animateToLocation(latLng: LatLng, zoom: Float) {
        CameraUpdateFactory.newLatLngZoom(latLng, zoom).let {
            mGoogleMap.animateCamera(it)
        }
    }

    private fun subscribeDrugStoresLocation() {
        mClusterManager.clearItems()
        drugStoreViewModel.drugStores.observe(this, Observer { drugStores ->
            drugStores?.let {
                //TODO: add worker to get data?
                Timber.e("observe drugStores count = ${drugStores.size}")
                //TODO: null check?
                //addMarkerToMap(drugStores)
                mClusterManager.addItems(it)
                mClusterManager.cluster()
            }
        })
    }

//    private fun addMarkerToMap(drugStores: List<DrugStore>) {
//        //TODO: 只取鄰近地點? bounds?
//        Timber.d("add drugstore marker ")
//        mGoogleMap.clear()
//        drugStores.forEach {
//            mGoogleMap.addMarker(
//                MarkerOptions().apply {
//                    position(LatLng(it.latitude, it.longitude))
//                    icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(it.maskAdult)))
//                    //TODO: zIndex?
//                }
//            ).tag = it
//        }
//    }

    private fun getMarkerIcon(adultMaskAmount: Int): Int {
        //TODO: 圖片多層問題?
        return when (adultMaskAmount) {
            0 -> R.drawable.mask_empty
            in 1 until 20 -> R.drawable.mask_few
            in 20 until 200 -> R.drawable.mask_less
            else -> R.drawable.mask_many
        }
    }

//    /**
//     * Demonstrates converting a [Drawable] to a [BitmapDescriptor],
//     * for use as a marker icon.
//     */
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

//    override fun onMarkerClick(marker: Marker): Boolean {
//        //TODO: like recycle view adapter, binding list item. use view model?
//        val drugstore = marker.tag as DrugStore
//        Timber.e("onMarkerClick = ${drugstore.name}")
//        binding.drugstore = drugstore
//        binding.drugstoreInfo.visibility = View.VISIBLE
//        return false
//    }

    override fun onMapClick(latlng: LatLng?) {
        Timber.e("onMapClick")
        //TODO: unbind here?
        //binding.drugstoreInfo.visibility = View.GONE
        binding.drugstore = null
    }

    override fun onClusterItemClick(item: DrugStore?): Boolean {
        item?.let {
            Timber.e("onClusterItemClick = ${it.name}")
            //animateToLocation(LatLng(it.latitude, it.longitude))
            binding.drugstore = it
        }
        return false
    }

    interface ChipCallback {
        //TODO: another way?
        fun onChipClick(view: View, drugstore: DrugStore?)
    }

    inner class DrugstoreRender :
        DefaultClusterRenderer<DrugStore>(applicationContext, mGoogleMap, mClusterManager) {
        override fun onBeforeClusterItemRendered(item: DrugStore, markerOptions: MarkerOptions) {
            val icon = getMarkerIcon(item.maskAdult)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(icon))
            super.onBeforeClusterItemRendered(item, markerOptions)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<DrugStore>): Boolean {
            return cluster.size > 10
        }
    }
}
