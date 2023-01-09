package com.example.myapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


import java.util.*

/**
 * This class adapting between the topScores fragment and the view
 */
private val FIRSTSCORE:Int = 0
private val SECONDSCORE:Int = 1
private val THIRDSCORE:Int = 2
class TopScoresAdapter(
    private val mContext: Context,
    mTopScores: MutableList<TopScores>
) :
    RecyclerView.Adapter<TopScoresAdapter.TopScoresViewHolder>() {
    private val mTopScores //list of all the scores
            : MutableList<TopScores>

    init {
        this.mTopScores = mTopScores
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopScoresViewHolder {
        //set  topScores fragment item layout
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.top_scores_item, parent, false)
        return TopScoresViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TopScoresViewHolder, position: Int) {
            val topScores: TopScores = mTopScores[position] //get the current score
        holder.text.text = topScores.firstName
        holder.title.text = topScores.totalScores.toString()

    }

    /**
     * This method return the amount of the scores in the list
     */
    override fun getItemCount(): Int {
        return mTopScores.size
    }

    /**
     * This class allows to set data to the score view objects
     */
    inner class TopScoresViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var profile: ImageView
        var title: TextView
        var text: TextView

        init {
            profile = itemView.findViewById<View>(R.id.top_score_user_profile) as ImageView
            title = itemView.findViewById<View>(R.id.top_score_user_name) as TextView
            text = itemView.findViewById<View>(R.id.top_scores_text) as TextView
        }
    }

}