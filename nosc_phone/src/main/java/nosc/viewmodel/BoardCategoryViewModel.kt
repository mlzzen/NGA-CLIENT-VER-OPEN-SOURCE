package nosc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gov.anzong.androidnga.base.util.ThreadUtils
import nosc.api.model.BoardModel
import sp.phone.mvp.model.entity.BoardCategory
/**
 * @author Yricky
 */
class BoardCategoryViewModel:ViewModel() {
    val boardCategoryList:MutableLiveData<List<BoardCategory>> by lazy {
        MutableLiveData()
    }
    fun query(){
        BoardModel.queryBoard {
            ThreadUtils.postOnMainThread {
                boardCategoryList.value = it
            }
        }
    }
}