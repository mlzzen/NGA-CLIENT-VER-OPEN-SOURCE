package nosc.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
//import java.time.format.DateTimeFormatter

/**
 * @author Yricky
 * @date 2022/4/3
 */
//private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
//private val dateFormatter = DateTimeFormatter.ofPattern("M-d")
//private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")


fun forumDateStringOf(l:Long):String{
    return dateStringOf(l*1000)
}

fun dateStringOf(l:Long):String{
    val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())
    val now = LocalDateTime.now()
    return if(ldt.year != now.year){
        "${ldt.year}-${ldt.month.value}-${ldt.dayOfMonth} ${ldt.hour}:${ldt.minute}"
    }else if(ldt.month != now.month || ldt.dayOfMonth != now.dayOfMonth){
        "${ldt.month.value}-${ldt.dayOfMonth} ${ldt.hour}:${ldt.minute}"
    }else "${ldt.hour}:${ldt.minute}"
}
