package nosc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import nosc.utils.ThreadUtils
import nosc.api.model.BoardModel
import sp.phone.mvp.model.entity.BoardCategory
/**
 * @author Yricky
 */
class BoardCategoryViewModel:ViewModel() {
    val boardCategoryList = MutableStateFlow<List<BoardCategory>>(emptyList())
    fun query(){
        BoardModel.requestBoard {
            ThreadUtils.postOnMainThread {
                boardCategoryList.value = it
            }
        }
    }
}