package nosc.utils

import com.alibaba.fastjson.JSON
import gov.anzong.androidnga.base.util.ContextUtils
import sp.phone.common.User
import sp.phone.common.UserManagerImpl
import sp.phone.mvp.model.entity.Board
import java.io.File

/**
 * @author Yricky
 * @date 2021/12/1
 */
object CurrentUserData{
    private const val FILE_BOOKMARK = "favouriteBoard.json"
    private const val FILE_RECENT = "recentBoard.json"

    val user:User get() = UserManagerImpl.getInstance().activeUser ?:User().also {
        it.userId = "notLogin"
    }
    private val externalDir:File get() =
        File(ContextUtils.getApplication().getExternalFilesDir("userData"),user.userId).also{
            if(!it.exists()){
                it.mkdirs()
            }
        }
    var favouriteBoard:List<Board>
    get() = try {
        parseArray(
            File(externalDir, FILE_BOOKMARK).readText(),
            Board::class.java
        )
    }catch (e:Throwable){
        listOf()
    }
    set(favBoard:List<Board>){
        File(externalDir, FILE_BOOKMARK).writeText(
            JSON.toJSONString(favBoard)
        )
    }
    val recentBoard:List<Board> get() = try {
        parseArray(
            File(externalDir, FILE_RECENT).readText(),
            Board::class.java
        )
    }catch (e:Throwable){
        listOf()
    }
}