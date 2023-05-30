package com.geochamp.myapp.View

import android.content.Context
import android.util.Log
import com.geochamp.myapp.Model.MarkerItem
import com.geochamp.myapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MarkerRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MarkerItem>
) : DefaultClusterRenderer<MarkerItem>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: MarkerItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val iconID = getMarkerIcon(item.title as String)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(iconID))
    }

    override fun onBeforeClusterRendered(cluster: Cluster<MarkerItem>, markerOptions: MarkerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.fromResource((R.drawable.locations)))
    }


    override fun getBucket(cluster: Cluster<MarkerItem>): Int {
        return cluster.size
    }

    private fun getMarkerIcon(name:String):Int {
        when(name){
            "Guess the flag" -> {
                return R.drawable.flagicon
            }
            "Calculator" -> {
                return R.drawable.calcuicon
            }
            "Clicker" -> {
                return R.drawable.clickericon
            }
            "Guess The City" -> {
                return R.drawable.cityicon
            }
            "Logo Challenge" -> {
                return R.drawable.logoicon
            }
            "Tap The Number" -> {
                return R.drawable.numbericon
            }
            "Destination" -> {
                return R.drawable.destination_ic
            }
            else -> {
                Log.e("icons", "Filed loading icon")
                return R.drawable.pinicon
            }

        }

    }


}