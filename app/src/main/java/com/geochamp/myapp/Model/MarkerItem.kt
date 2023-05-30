package com.geochamp.myapp.Model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

 class MarkerItem(
    lat: Double,
    lng: Double,
    id: String,
    title: String,
    snippet: String
) : ClusterItem {

    private val position: LatLng
    private val title: String
    private val snippet: String
    val id: String

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    init {
        position = LatLng(lat, lng)
        this.title = title
        this.snippet = snippet
        this.id = id
    }
}