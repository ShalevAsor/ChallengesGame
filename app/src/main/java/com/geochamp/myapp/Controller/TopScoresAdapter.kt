package com.geochamp.myapp.Controller

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.R
import com.squareup.picasso.Picasso


import java.util.*

/**
 * This class representing the adapter for the `TopScores` recycler view.
 *
 * @property mContext the context of the `TopScores` fragment
 * @property mTopScores the list of scores to display
 */
class TopScoresAdapter(
    private val mContext: Context,
    mTopScores: MutableList<ScoreModel>
) :
    RecyclerView.Adapter<TopScoresAdapter.TopScoresViewHolder>() {
    private val mTopScores //list of all the scores
            : MutableList<ScoreModel>

    init {
        this.mTopScores = mTopScores
    }

    /**
     * Creates a new view holder when a new row is needed.
     *
     * @param parent The parent view group.
     * @param viewType The view type of the new view.
     * @return The view holder that holds the view for a single row.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopScoresViewHolder {
        //set  topScores fragment item layout
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.top_scores_item, parent, false)
        return TopScoresViewHolder(view)
    }


    /**
     * Binds the data to the views in the view holder.
     *
     * @param holder The view holder that holds the views for a single row.
     * @param position The position of the current row.
     */

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TopScoresViewHolder, position: Int) {
        val topScores: ScoreModel = mTopScores[position] //get the current score
        holder.text.text = topScores.firstName
        holder.title.text = topScores.total_score.toString()
        Log.d("topscoresVals", "User created in the the value is -  ${topScores.total_score}.")
        if(!topScores.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(topScores.imageUrl).into(holder.profile)
        }
    }

    /**
     * Returns the number of scores in the list.
     *
     * @return The number of scores in the list.
     */
    override fun getItemCount(): Int {
        return mTopScores.size
    }

    /**
     * This class that holds the views for a single row in the list.
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