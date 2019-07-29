package ru.gonchar17narod.memocats.presentation

import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import com.android.gonchar17narod.memocats.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import ru.gonchar17narod.memocats.model.Tile
import java.util.*

class MainActivity : AppCompatActivity(), Callback {

    private lateinit var mInterstitialAd: InterstitialAd

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)


        MobileAds.initialize(this)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = resources.getString(R.string.ad_id)

        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {

            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                super.onAdClosed()
            }
        }

        restart()
    }

    fun restart() {
        val tiles = getTiles()

        val tileAdapter = TileAdapter(this, tiles, this, 7)

        val columns = 2
        val rows = tiles.size / columns

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val height = displayMetrics.heightPixels / rows
        val margin = ((displayMetrics.widthPixels - (height * columns)) / (columns * 3))
        val params = ConstraintLayout.LayoutParams(height, height)
        params.setMargins(margin, 0, margin, 0)
        val recyclerParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recyclerParams.setMargins(margin, 0, margin, 0)
        recyclerView.layoutParams = recyclerParams
        tileAdapter.setParams(params)
        recyclerView.adapter = tileAdapter
        (recyclerView.adapter as TileAdapter).notifyDataSetChanged()
        recyclerView.layoutManager = GridLayoutManager(this, columns)
    }

    fun getTiles(): ArrayList<Tile> {
        val cards = ArrayList<Tile>()
        cards.add(Tile(R.drawable.cat_green))
        cards.add(Tile(R.drawable.cat_grey))
        cards.add(Tile(R.drawable.cat_silver))
        cards.add(Tile(R.drawable.cat_pink))
        cards.addAll(cards.map { it.copy() })
        cards.shuffle()
        return cards
    }

    override fun onWin() {
        GameDialog.newInstance(R.layout.outcome_win).setOnDismissListener(DialogInterface.OnDismissListener { restart() }).show(fragmentManager, null)
    }

    override fun onLose() {
        GameDialog.newInstance(R.layout.outcome_lose).setOnDismissListener(DialogInterface.OnDismissListener { restart() }).show(fragmentManager, null)
    }

    override fun showAd() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }
}

interface Callback {
    fun onWin()
    fun onLose()
    fun showAd()
}

class GameDialog : DialogFragment() {

    companion object {
        fun newInstance(outcome: Int): GameDialog {
            val frag = GameDialog()
            val args = Bundle()
            args.putInt("outcome", outcome);
            frag.arguments = args
            return frag
        }
    }

    private lateinit var dismissListener: DialogInterface.OnDismissListener

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener): GameDialog {
        dismissListener = listener
        return this
    }

    override fun onDismiss(dialog: DialogInterface?) {
        dismissListener.onDismiss(dialog)
        super.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = activity.layoutInflater.inflate(arguments.getInt("outcome"), null)
        view.setOnClickListener { dismiss() }
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(view)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}
