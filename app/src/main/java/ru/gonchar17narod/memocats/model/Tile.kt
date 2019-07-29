package ru.gonchar17narod.memocats.model

import com.wajahatkarim3.easyflipview.EasyFlipView


data class Tile (
  val imageIndex: Int
) {
  var flipView: EasyFlipView? = null
}
