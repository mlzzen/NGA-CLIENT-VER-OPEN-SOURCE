package nosc.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author Yricky
 * @date 2022/4/3
 */
private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
private val dateFormatter = DateTimeFormatter.ofPattern("M-d")
private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")


fun dateStringOf(l:Long):String{
    val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(l*1000), ZoneId.systemDefault())
    val now = LocalDateTime.now()
    return if(ldt.year != now.year){
        "${ldt.format(yearFormatter)}-${ldt.format(dateFormatter)} ${ldt.format(timeFormatter)}"
    }else if(ldt.month != now.month || ldt.dayOfMonth != now.dayOfMonth){
        "${ldt.format(dateFormatter)} ${ldt.format(timeFormatter)}"
    }else ldt.format(timeFormatter)
}