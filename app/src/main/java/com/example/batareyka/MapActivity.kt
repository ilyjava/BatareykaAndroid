package com.example.batareyka

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.PointF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider.fromResource


class MapActivity : AppCompatActivity(), UserLocationObjectListener, CameraListener {
    private lateinit var userLocationLayer: UserLocationLayer

    private var routeStartLocation = Point(0.0, 0.0)

    private var permissionLocation = false
    private var followUserLocation = false

    lateinit var mapView: MapView
    lateinit var floatingActionButton: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(mapKitApiKey)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_map)
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.mapview)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        checkPermission()

        userInterface()
    }

    private fun checkPermission() {
        val permissionLocation = checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (permissionLocation != PERMISSION_GRANTED) {
            requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), requestPermissionLocation)
        } else {
            onMapReady()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    onMapReady()
                }

                return
            }
        }
    }

    private fun userInterface() {
        val mapLogoAlignment = Alignment(LEFT, BOTTOM)
        mapView.map.logo.setAlignment(mapLogoAlignment)

        floatingActionButton.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()

                followUserLocation = true
            } else {
                checkPermission()
            }
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        mapView.map.addCameraListener(this)

        cameraUserPosition()

        permissionLocation = true
    }

    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStartLocation = userLocationLayer.cameraPosition()!!.target
            mapView.map.isRotateGesturesEnabled = false
            mapView.map.move(CameraPosition(Point(0.0, 0.0), 14f, 0f, 0f))
        } else {
            mapView.map.move(CameraPosition(Point(55.75, 36.61), 16f, 0f, 0f))
        }
    }

    override fun onCameraPositionChanged(p0: Map, p1: CameraPosition, p2: CameraUpdateReason, finish: Boolean) {
        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
                PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
                PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )

        floatingActionButton.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        floatingActionButton.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        userLocationView.pin.setIcon(fromResource(this, R.drawable.user_arrow))
        userLocationView.arrow.setIcon(fromResource(this, R.drawable.user_arrow))
        userLocationView.accuracyCircle.fillColor = Color.argb(70, 51, 204,51)
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    override fun onObjectRemoved(p0: UserLocationView) {}

    companion object {
        /**
         * Replace "your_api_key" with a valid developer key.
         * You can get it at the https://developer.tech.yandex.ru/ website.
         */
        const val mapKitApiKey = "5a4c772a-b846-4564-b9c6-dd616f4c5a8d"
        const val requestPermissionLocation = 1
    }


    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }
}