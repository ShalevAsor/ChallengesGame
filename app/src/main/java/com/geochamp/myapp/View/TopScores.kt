package com.geochamp.myapp.View

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geochamp.myapp.Controller.TopScoresAdapter
import com.geochamp.myapp.Controller.TopScoresController
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView


/**
 * This class represents a collection of top scores
 */
//class TopScores : Fragment() {
//
//    private lateinit var mContext:Context
//    var firstName: String? = null
//    var totalScores: Long? = 0
//    var imageUrl: String? = null
//    private lateinit var drawerLayout:DrawerLayout
//    //database
//    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var dbRef: DatabaseReference
//
//    //tools
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var topScoresAdapter: TopScoresAdapter
//    private lateinit var topScoresController:TopScoresController
//    private lateinit var topScores: MutableList<ScoreModel>
//    private lateinit var userName:TextView
//    private lateinit var myScore:TextView
//    private lateinit var myImageProfile:ImageView
//    private lateinit var top3Scores: MutableList<ScoreModel>
//    private var userScore:Int = 0
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        /* set the top scores fragment layout and the recycler view */
//        val view: View = inflater.inflate(R.layout.fragment_top_scores, container, false)
//        mContext = context as Context
//        topScoresController = TopScoresController(this)
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        setHasOptionsMenu(true)
//        recyclerView = view.findViewById(R.id.recycler_view)
//        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        userName = view.findViewById<TextView>(R.id.my_top_score_user_name)
//        myScore = view.findViewById<TextView>(R.id.my_top_scores_text)
//        myImageProfile = view.findViewById<CircleImageView>(R.id.my_top_score_user_profile)
//
//        //set user name and score here
//        firebaseAuth = FirebaseAuth.getInstance()
//        val userID = firebaseAuth.currentUser?.uid
//        if (userID != null) {
//            topScoresController.getUser(userID){user ->
//                if (user != null) {
//                    firstName  = user.firstName
//                    imageUrl = user.imageUrl
//                    totalScores = user.personalScore
//                    Log.d("signgs", "the valueis - $totalScores.")
//                    userName.text = user.firstName + " " + user.lastName
//                    myScore.text = user.personalScore.toString()
//                    loadImage(user.imageUrl, myImageProfile)
//                }
//            }
//        }
////        userName.text = "My user name"
////        myScore.text = "35 Points"
//
//        /* init the top scores list and top scores adapter */
//        topScores = ArrayList()
//        top3Scores = ArrayList()
//        topScoresAdapter = TopScoresAdapter(context as Context, topScores)
//        recyclerView.adapter = topScoresAdapter
//
//        /* read the scores from the database */
//        //(activity as AppCompatActivity?)!!.supportActionBar!!.hide()
//        topScoresController.readTopScores(topScores, topScoresAdapter)
//        val firstScoreUserName =
//            view.findViewById<TextView>(R.id.my_top_score_user_name_first_score)
//        val secondScoreUserName =
//            view.findViewById<TextView>(R.id.my_top_score_user_name_second_score)
//        val thirdScoreUserName =
//            view.findViewById<TextView>(R.id.my_top_score_user_name_third_score)
//        val firstScore = view.findViewById<TextView>(R.id.my_top_scores_text_first_score)
//        val secondScore = view.findViewById<TextView>(R.id.my_top_scores_text_second_score)
//        val thirdScore = view.findViewById<TextView>(R.id.my_top_scores_text_third_score)
//        val firstImage =
//            view.findViewById<CircleImageView>(R.id.my_top_score_user_profile_first_score)
//        val secondImage =
//            view.findViewById<CircleImageView>(R.id.my_top_score_user_profile_second_score)
//        val thirdImage =
//            view.findViewById<CircleImageView>(R.id.my_top_score_user_profile_third_score)
//        //add image profile
//        //set top 3 players
//        val scoresRef = FirebaseDatabase.getInstance().getReference("Billboard")
//        topScoresController.getTop3Scores { top3Scores ->
//            if (top3Scores.isNotEmpty()) {
//                val firstScoreObj = top3Scores[0]
//                val secondScoreObj = top3Scores[1]
//                val thirdScoreObj = top3Scores[2]
//                firstScore.text = firstScoreObj.total_score.toString()
//                secondScore.text = secondScoreObj.total_score.toString()
//                thirdScore.text = thirdScoreObj.total_score.toString()
//                firstScoreUserName.text = firstScoreObj.firstName
//                secondScoreUserName.text = secondScoreObj.firstName
//                thirdScoreUserName.text = thirdScoreObj.firstName
//                loadImage(firstScoreObj.imageUrl, firstImage)
////                Log.d("billboardvalue1", "User created in the database$top3Scores.")
////                Log.d("billboardvalue1", "User created in the database${firstScoreObj.totalScore}.")
////                Log.d("billboardvalue2", "User created in the database${secondScoreObj.totalScore}.")
////                Log.d("billboardvalue3", "User created in the database${thirdScoreObj.totalScore}.")
//                loadImage(secondScoreObj.imageUrl, secondImage)
//                loadImage(thirdScoreObj.imageUrl, thirdImage)
//            }
//        }
//        return view
//    }
//
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//    }
//
////    private fun loadImage(imageUrl:String?,image:ImageView ){
////
////        if (imageUrl.isNullOrEmpty()) {
////            Glide.with(this)
////                .load(R.drawable.ic_person)
////                .into(image)
////        }
////        else{
////            Glide.with(this)
////                .load(imageUrl)
////                .into(image)
////        }
////
////    }
//override fun onOptionsItemSelected(item: MenuItem): Boolean {
//    when (item.itemId) {
//        android.R.id.home -> {
//            activity?.onBackPressed()
//            return true
//        }
//    }
//    return super.onOptionsItemSelected(item)
//}
//
//    private fun loadImage(imageUrl: String?, imageView: ImageView) {
//        Glide.with(mContext)
//            .load(imageUrl)
//            .centerCrop()
//            .placeholder(R.drawable.ic_person)
//            .error(R.drawable.ic_person)
//            .into(imageView)
//    }
//
////    override fun onOptionsItemSelected(item: MenuItem): Boolean {
////        when (item.itemId) {
////            android.R.id.home -> {
////                // handle back arrow press
////                fragmentManager?.popBackStack()
////                return true
////            }
////        }
////        return super.onOptionsItemSelected(item)
////    }
//override fun onActivityCreated(savedInstanceState: Bundle?) {
//    super.onActivityCreated(savedInstanceState)
//    val activity = requireActivity()
//    if (activity is AppCompatActivity) {
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
//    }
//}
//
//
//}
class TopScores : AppCompatActivity() {

        private lateinit var mContext:Context
    var firstName: String? = null
    var totalScores: Long? = 0
    var imageUrl: String? = null
    //database
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    //tools
    private lateinit var recyclerView: RecyclerView
    private lateinit var topScoresAdapter: TopScoresAdapter
    private lateinit var topScoresController:TopScoresController
    private lateinit var topScores: MutableList<ScoreModel>
    private lateinit var userName:TextView
    private lateinit var myScore:TextView
    private lateinit var myImageProfile:ImageView
    private lateinit var top3Scores: MutableList<ScoreModel>
    private var userScore:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_top_scores)
        /* set the top scores fragment layout and the recycler view */
//        val view: View = inflater.inflate(R.layout.fragment_top_scores, container, false)
         mContext = this
        topScoresController = TopScoresController(this)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        userName = findViewById<TextView>(R.id.my_top_score_user_name)
        myScore = findViewById<TextView>(R.id.my_top_scores_text)
        myImageProfile = findViewById<CircleImageView>(R.id.my_top_score_user_profile)
//        supportActionBar?.title = "Geo-Champ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set user name and score here
        firebaseAuth = FirebaseAuth.getInstance()
        val userID = firebaseAuth.currentUser?.uid
        if (userID != null) {
            topScoresController.getUser(userID){user ->
                if (user != null) {
                    firstName  = user.firstName
                    imageUrl = user.imageUrl
                    totalScores = user.personalScore
                    Log.d("signgs", "the valueis - $totalScores.")
                    userName.text = user.firstName + " " + user.lastName
                    myScore.text = user.personalScore.toString()
                    loadImage(user.imageUrl, myImageProfile)
                }
            }
        }
//        userName.text = "My user name"
//        myScore.text = "35 Points"

        /* init the top scores list and top scores adapter */
        topScores = ArrayList()
        top3Scores = ArrayList()
        topScoresAdapter = TopScoresAdapter(mContext, topScores)
        recyclerView.adapter = topScoresAdapter

        /* read the scores from the database */
        //(activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        topScoresController.readTopScores(topScores, topScoresAdapter)
        val firstScoreUserName =
            findViewById<TextView>(R.id.my_top_score_user_name_first_score)
        val secondScoreUserName =
            findViewById<TextView>(R.id.my_top_score_user_name_second_score)
        val thirdScoreUserName =
            findViewById<TextView>(R.id.my_top_score_user_name_third_score)
        val firstScore = findViewById<TextView>(R.id.my_top_scores_text_first_score)
        val secondScore = findViewById<TextView>(R.id.my_top_scores_text_second_score)
        val thirdScore = findViewById<TextView>(R.id.my_top_scores_text_third_score)
        val firstImage =
            findViewById<CircleImageView>(R.id.my_top_score_user_profile_first_score)
        val secondImage =
            findViewById<CircleImageView>(R.id.my_top_score_user_profile_second_score)
        val thirdImage =
            findViewById<CircleImageView>(R.id.my_top_score_user_profile_third_score)
        //add image profile
        //set top 3 players
        val scoresRef = FirebaseDatabase.getInstance().getReference("Billboard")
        topScoresController.getTop3Scores { top3Scores ->
            if (top3Scores.isNotEmpty()) {
                val firstScoreObj = top3Scores[0]
                val secondScoreObj = top3Scores[1]
                val thirdScoreObj = top3Scores[2]
                firstScore.text = firstScoreObj.total_score.toString()
                secondScore.text = secondScoreObj.total_score.toString()
                thirdScore.text = thirdScoreObj.total_score.toString()
                firstScoreUserName.text = firstScoreObj.firstName
                secondScoreUserName.text = secondScoreObj.firstName
                thirdScoreUserName.text = thirdScoreObj.firstName
                loadImage(firstScoreObj.imageUrl, firstImage)
//                Log.d("billboardvalue1", "User created in the database$top3Scores.")
//                Log.d("billboardvalue1", "User created in the database${firstScoreObj.totalScore}.")
//                Log.d("billboardvalue2", "User created in the database${secondScoreObj.totalScore}.")
//                Log.d("billboardvalue3", "User created in the database${thirdScoreObj.totalScore}.")
                loadImage(secondScoreObj.imageUrl, secondImage)
                loadImage(thirdScoreObj.imageUrl, thirdImage)
            }
        }

    }

        private fun loadImage(imageUrl: String?, imageView: ImageView) {
        Glide.with(mContext)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(imageView)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}