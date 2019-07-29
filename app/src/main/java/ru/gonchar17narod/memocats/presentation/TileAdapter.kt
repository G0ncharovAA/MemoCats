package ru.gonchar17narod.memocats.presentation

import android.content.Context
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.gonchar17narod.memocats.R
import com.android.gonchar17narod.memocats.R.string
import com.wajahatkarim3.easyflipview.EasyFlipView
import kotlinx.android.synthetic.main.card_front.view.*
import ru.gonchar17narod.memocats.model.Tile
import java.util.*

class TileAdapter(
        private val context: Context,
        private val tileList: ArrayList<Tile>,
        private val callback: Callback,
        private val tryLimit: Int
) : RecyclerView.Adapter<TileAdapter.CardViewHolder>() {

    companion object {
        private val AD_LIMIT = 9
        private var adCount = AD_LIMIT
    }

    private var params: ConstraintLayout.LayoutParams? = null
    internal val handler = Handler()

    private var tilesFlipped = 0
    private var positionPreviousCard = 0
    private var matchesCount = 0
    private var tryCount = 0
    private var touchTempDisabled = false

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): CardViewHolder {
        return CardViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item, parent, false))
    }

    override fun onBindViewHolder(
            holder: CardViewHolder,
            position: Int
    ) {
        tileList[position].flipView = holder.flipView
        holder.textView.text = context.getString(string.question_mark)
        holder.rootLayout.layoutParams = params
        holder.tileImageView.setImageResource(tileList[position].imageIndex)
        holder.flipView.setOnClickListener(View.OnClickListener {
            if (touchTempDisabled)
                return@OnClickListener
            it as EasyFlipView
            if (!it.isFlipEnabled)
                return@OnClickListener
            it.flipTheView()
            tilesFlipped++
            if (tilesFlipped == 1) {
                positionPreviousCard = position
                tileList[position]
                        .flipView!!.isFlipEnabled = false
            } else if (tilesFlipped == 2 && position != positionPreviousCard) {
                // Disable card flipping while 2 cards are flipped to prevent >2 cards flipped
                // Especially for during animation below
                touchTempDisabled = true
                val prevCard = tileList[positionPreviousCard]
                val currCard = tileList[position]
                if (prevCard.imageIndex == currCard.imageIndex) {
                    tileList[positionPreviousCard]
                            .flipView!!.isFlipEnabled = false
                    tileList[position]
                            .flipView!!.isFlipEnabled = false
                    touchTempDisabled = false

                    // If the number of matches is equal to the number of pairs of cards then win
                    matchesCount++
                    if (matchesCount == itemCount / 2) {
                        callback.onWin()
                        return@OnClickListener
                    }
                } else {
                    tileList[positionPreviousCard]
                            .flipView!!.isFlipEnabled = true
                    tileList[position]
                            .flipView!!.isFlipEnabled = true
                    handler.postDelayed({
                        tileList[positionPreviousCard].flipView!!.flipTheView()
                        it.flipTheView()
                        touchTempDisabled = false
                    }, 700)
                }

                tilesFlipped = 0

                adCount--
                if (adCount < 1) {
                    callback.showAd()
                    adCount = AD_LIMIT
                }
                tryCount++
                if (tryCount >= tryLimit) {
                    callback.onLose()
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return tileList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.cardViewTextView as TextView
        val rootLayout = itemView.findViewById(R.id.cardViewRootLayout) as RelativeLayout
        val flipView = itemView.findViewById(R.id.flipView) as EasyFlipView
        val tileImageView = itemView.findViewById(R.id.cardImageView) as ImageView
    }

    fun setParams(params: ConstraintLayout.LayoutParams) {
        this.params = params
    }
}
