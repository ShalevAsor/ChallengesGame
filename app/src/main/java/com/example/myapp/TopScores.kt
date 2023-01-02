package com.example.myapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class represents score in the Top Scores list
 */
class TopScores : Fragment() {

    var firstName: String? = null
    var totalScores: Int = 0

    //tools
    private lateinit var recyclerView: RecyclerView
    private lateinit var topScoresAdapter: TopScoresAdapter
    private lateinit var topScores: MutableList<TopScores>
    private lateinit var userName:TextView
    private lateinit var myScore:TextView
    private var userScore:Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        /* set the top scores fragment layout and the recycler view */
        val view: View = inflater.inflate(R.layout.fragment_top_scores, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        userName = view.findViewById<TextView>(R.id.my_top_score_user_name)
        myScore = view.findViewById<TextView>(R.id.my_top_scores_text)
        //set user name and score here
        userName.text = "My user name"
        myScore.text = "35 Points"
        /* init the top scores list and top scores adapter */
        topScores = ArrayList()
        topScoresAdapter = TopScoresAdapter(context as Context, topScores )
        recyclerView.adapter = topScoresAdapter

        /* read the scores from the database */
        readTopScores(topScores, topScoresAdapter)
        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun readTopScores(
        topScores: MutableList<TopScores>,
        topScoresAdapter: TopScoresAdapter
    ) {
        /* get the data of each score in the Billboard aka top scores */
        FirebaseDatabase.getInstance().reference.child("Billboard")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /* if some of the scores has changed then it will update the adapter */
                    for (dss in snapshot.children) {
                        val billboard: TopScores? = dss.getValue(TopScores::class.java)
                        if (billboard != null) {
                            topScores.add(billboard)
                        }
                    }
                    topScores.reverse()
                    topScoresAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}