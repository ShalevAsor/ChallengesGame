package com.example.myapp.Model

data class MarkerModel(

    var marker_id:String?= null,
    var chall_name: String? = null,
    var chall_description:String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var top_score: Int? = null,
    var personal_score: Int?=null
)