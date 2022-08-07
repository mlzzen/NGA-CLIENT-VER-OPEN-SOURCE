package gov.anzong.androidnga.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import sp.phone.view.RecyclerViewEx
import sp.phone.ui.adapter.BoardSubListAdapter
import sp.phone.mvp.model.entity.SubBoard
import sp.phone.task.SubscribeSubBoardTask
import sp.phone.param.ParamKey
import androidx.recyclerview.widget.LinearLayoutManager
import nosc.api.callbacks.OnHttpCallBack
import com.alibaba.android.arouter.launcher.ARouter
import gov.anzong.androidnga.R
import gov.anzong.androidnga.arouter.ARouterConstants
import nosc.utils.showTopicList
import nosc.utils.uxUtils.ToastUtils

/**
 * Created by Justwen on 2018/1/27.
 */
class BoardSubListFragment : BaseRxFragment(){
    private var mListView: RecyclerViewEx? = null
    private var mListAdapter: BoardSubListAdapter? = null
    private var mBoardList: List<SubBoard>? = null
    private var mSubscribeTask: SubscribeSubBoardTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val bundle = arguments
        setTitle(String.format("%s - 子板块", bundle?.getString(ParamKey.KEY_TITLE)))
        mBoardList = bundle?.getParcelableArrayList("subBoard")
        mSubscribeTask = SubscribeSubBoardTask(lifecycleProvider)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mListView = RecyclerViewEx(inflater.context)
        mListView?.layoutManager = LinearLayoutManager(inflater.context)
        mListAdapter = BoardSubListAdapter(inflater.context, mBoardList)
        mListAdapter?.setOnClickListener{ v->
            val board = v.tag as SubBoard
            if (v.id == R.id.check) {
                val callBack: OnHttpCallBack<String> = object : OnHttpCallBack<String> {
                    override fun onError(text: String) {
                        ToastUtils.error(text)
                        (v as Checkable).isChecked = board.isChecked
                    }

                    override fun onSuccess(data: String) {
                        ToastUtils.success(data)
                        board.isChecked = v.isClickable
                        setResult(Activity.RESULT_OK)
                    }
                }
                if (board.isChecked) {
                    mSubscribeTask!!.unsubscribe(board, callBack)
                } else {
                    mSubscribeTask!!.subscribe(board, callBack)
                }
            } else {
                context?.showTopicList(board)
            }
        }
        mListView?.adapter = mListAdapter
        return mListView
    }
}