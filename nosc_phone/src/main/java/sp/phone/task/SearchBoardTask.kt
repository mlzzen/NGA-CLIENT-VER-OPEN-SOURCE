package sp.phone.task

import sp.phone.mvp.model.BoardModel.findBoard
import nosc.api.callbacks.OnSimpleHttpCallBack
import sp.phone.mvp.model.entity.Board
import nosc.api.retrofit.RetrofitHelper
import io.reactivex.schedulers.Schedulers
import com.alibaba.fastjson.JSON
import sp.phone.mvp.model.BoardModel
import io.reactivex.android.schedulers.AndroidSchedulers
import sp.phone.rxjava.BaseSubscriber
import sp.phone.util.StringUtils
import java.lang.Exception

/**
 * Created by Justwen on 2018/10/12.
 */
object SearchBoardTask {
    @JvmStatic
    fun execute(boardName: String, callBack: OnSimpleHttpCallBack<Board?>) {
        RetrofitHelper.getInstance()
            .service["http://bbs.nga.cn/forum.php?&__output=8&key=" + StringUtils.encodeUrl(
            boardName,
            "gbk"
        )]
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { s: String? ->
                try {
                    val obj = JSON.parseObject(s).getJSONObject("data").getJSONObject("0")
                    val fid = obj.getInteger("fid")
                    val title = obj.getString("name")
                    var board = findBoard(fid.toString())
                    if (board == null) {
                        board = Board(fid, title)
                    }
                    return@map board
                } catch (e: Exception) {
                }
                null
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : BaseSubscriber<Board?>() {
                override fun onNext(board: Board) {
                    callBack.onResult(board)
                }

                override fun onError(throwable: Throwable) {
                    callBack.onResult(null)
                }
            })
    }
}