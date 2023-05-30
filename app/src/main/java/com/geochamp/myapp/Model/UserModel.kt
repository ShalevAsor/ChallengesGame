package com.geochamp.myapp.Model
/**
 * Data class representing the user model with user information
 * @property firstName the first name of the user
 * @property lastName the last name of the user
 * @property userEmail the email address of the user
 * @property imageUrl the profile image URL of the user
 * @property personalScore the personal score of the user
 * @property pass the password of the user
 */
data class UserModel(
    var firstName: String? = null,
    var lastName: String? = null,
    var userEmail: String? = null,
    var imageUrl: String? = null,
    var personalScore: Long? = null,
    var pass: String? = null,
    var challengesCreated: Int? = 0,
    var challengesPlayed: Int? = 0,
    var pointsEarned:Long?=0,
    var pointSpent:Int?=0,

) {

}