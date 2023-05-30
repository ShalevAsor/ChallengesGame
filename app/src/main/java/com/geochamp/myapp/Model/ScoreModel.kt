package com.geochamp.myapp.Model

/**
 * ScoreModel class represents the data of an individual score of a user that stores on Billboard
 * @property firstName The first name of the user
 * @property total_score The total score of the user
 * @property imageUrl The url of the user's profile image
 *
 */
data class ScoreModel(
    var firstName: String? = null,
    var total_score : Long? = 0,
    var imageUrl: String? = null
)