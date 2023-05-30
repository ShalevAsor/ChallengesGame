package com.geochamp.myapp.Model

/**
 * This class describes the properties of a marker.
 * @property marker_id The unique id of the marker.
 * @property chall_name The name of the challenge associated with this marker.
 * @property chall_description The description of the challenge associated with this marker.
 * @property lat The latitude of the marker location.
 * @property long The longitude of the marker location.
 * @property top_score The top score achieved for the challenge associated with this marker.
 * @property time_to_live The time period for which the marker will remain active.
 */
data class MarkerModel(

    var marker_id:String?= null,
    var challenge_creator:String?=null,
    var chall_name: String? = null,
    var chall_description:String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var top_score: Int? = null,
    var time_to_live: Long?=null
)