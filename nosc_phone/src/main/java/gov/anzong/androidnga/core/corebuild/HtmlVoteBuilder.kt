package gov.anzong.androidnga.core.corebuild

import gov.anzong.androidnga.core.data.HtmlData
import java.lang.ref.SoftReference

/**
 * Created by Justwen on 2018/8/28.
 */
class HtmlVoteBuilder : IHtmlBuild {
    companion object{
        const val TYPE_RATE = "2"
        const val TYPE_RATE_APPLY = "3"
    }
    override fun build(htmlData: HtmlData): CharSequence {
        if(htmlData.vote.isNullOrEmpty())
            return ""
        return "<div class='collapse'>" + Vote(htmlData.vote.split("~")).htmlStr + "</div>"
    }

    private class Vote(vList:List<String>){
        private object Votes{
            private var voteMap = SoftReference(HashMap<Int,Vote>())
            operator fun get(id:Int):Vote?{
                if(voteMap.get() == null)
                    voteMap = SoftReference(HashMap())
                return voteMap.get()?.get(id)
            }

            fun put(id:Int,vote:Vote){
                if(voteMap.get() == null)
                    voteMap = SoftReference(HashMap())
                voteMap.get()?.put(id,vote)
            }
        }
        class VoteItem(var name:String,var voterCount:Int,var totalRate:Int)
        val htmlStr:String
        var type:String = "0"
        private val itemMap = HashMap<Int,VoteItem>()
        init {
            val map = HashMap<String,String>()
            val sb = StringBuilder()
            if(vList.size%2 == 0)
                vList.forEachIndexed { index, s ->
                    if(index%2 == 0){
                        map[s] = vList[index+1]
                    }
                }
            map.toSortedMap().forEach {
                it.key.toIntOrNull()?.also { id ->
                    if(Votes[id] == null){
                        itemMap[id] = VoteItem(it.value,0,0)
                        Votes.put(id,this)
                    } else
                        itemMap[id] = VoteItem(it.value,0,0)
                    return@forEach
                }
                if(it.key.startsWith("_")){
                    it.key.substring(1).toIntOrNull().also { id->
                        val rate = it.value.split(",")
                        val vi = itemMap[id]
                        vi?.voterCount = rate[0].toIntOrNull() ?: 0
                        vi?.totalRate = rate[1].toIntOrNull() ?: 0
                    }
                    return@forEach
                }
                when(it.key){
                    "type" -> type = it.value
                }
            }
            when(type){
                "0" -> {
                    itemMap.forEach { kv ->
                        sb.append(kv.value.name).append("(${kv.value.voterCount}人)").append("<br/>")
                    }
                }
                "2" ->{
                    itemMap.forEach { kv ->
                        sb.append(kv.value.name).append(" ${kv.value.voterCount}人给出均分${String.format("%.2f",kv.value.totalRate*1.0/kv.value.voterCount.coerceAtLeast(1))}").append("<br/>")
                    }
                }
                "3" ->{
                    itemMap.forEach { kv ->
                        sb.append(Votes[kv.key]?.itemMap?.get(kv.key)?.name).append("${kv.value.name}分").append("<br/>")
                    }
                }
            }
            htmlStr = sb.toString()
        }
    }

}