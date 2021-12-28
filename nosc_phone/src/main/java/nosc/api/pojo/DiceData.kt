package nosc.api.pojo

import kotlin.math.floor

class DiceData (
    val txt: String,
    val authorId:Int,
    val tId:Int,
    val pId:Int,
    val seedOffset:Int = 0,
    val rndSeed:Double = 0.0,
        ){
    val id: String = "postcontent0"
    val seed:Double = 2110032.0
    var argsId: String = id ?: randDigi("bbcode", 10000)
}

fun randDigi(p: String, l: Int): String {
    return p + floor(Math.random() * l)
}

fun DiceData.rnd(): Double {
    val r:Double = if (rndSeed == 0.0) {
        (authorId + tId + pId + (if (tId > 10246184 || pId > 200188932) seedOffset else 0)).toDouble()
//        if (rndSeed == 0.0) rndSeed = Math.floor(Math.random() * 10000)
    } else rndSeed
    return ((r * 9301 + 49297) % 233280) / 233280.0
//    seed = (seed * 9301 + 49297) % 233280
//    seed = seed
//    return seed / 233280.0
}