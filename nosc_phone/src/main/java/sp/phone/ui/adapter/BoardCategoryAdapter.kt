package sp.phone.ui.adapter

import android.app.Activity
import sp.phone.mvp.model.entity.BoardCategory
import androidx.recyclerview.widget.RecyclerView
import sp.phone.ui.adapter.BoardCategoryAdapter.BoardViewHolder
import gov.anzong.androidnga.R
import android.widget.TextView
import android.view.ViewGroup
import sp.phone.mvp.model.entity.Board
import nosc.api.constants.ApiConstants
import gov.anzong.androidnga.GlideApp
import sp.phone.rxjava.RxUtils
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import nosc.api.model.BoardModel
import java.util.ArrayList

/**
 * 版块Grid Adapter
 */
class BoardCategoryAdapter(private val mActivity: Activity, private val mCategory: BoardCategory) :
    RecyclerView.Adapter<BoardViewHolder>() {
    private var mTitlePositions: MutableList<Int>  = ArrayList()
    private val mTotalCount:Int get(){
        mTitlePositions.clear()
        return if (mCategory.subCategoryList != null) {
            var count = 0
            for (subCategory in mCategory.subCategoryList) {
                mTitlePositions.add(count)
                count += subCategory.boardList.size
                count++
            }
            count
        } else {
            mCategory.boardList.size
        }
    }

    class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icon: ImageView = itemView.findViewById(R.id.icon_board_img)

        var name: TextView = itemView.findViewById(R.id.text_board_name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val layoutId =
            if (viewType == BOARD_ITEM) R.layout.list_board_item else R.layout.list_board_category_item
        val view = layoutInflater.inflate(layoutId, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        if (getItemViewType(position) == BOARD_ITEM) {
            val board: Board = if (mTitlePositions.isNotEmpty()) {
                var subCategoryIndex = 0
                while (subCategoryIndex < mTitlePositions.size) {
                    if (mTitlePositions[subCategoryIndex] > position) {
                        break
                    }
                    subCategoryIndex++
                }
                subCategoryIndex--
                val realPosition = position - mTitlePositions[subCategoryIndex] - 1
                val subCategory = mCategory.getSubCategory(subCategoryIndex)
                subCategory.getBoard(realPosition)
            } else {
                mCategory.getBoard(position)
            }


            //设置版面图标
            val url: String = if (board.stid != 0) {
                String.format(ApiConstants.URL_BOARD_ICON_STID, board.stid)
            } else {
                String.format(ApiConstants.URL_BOARD_ICON, board.fid)
            }
            GlideApp.with(mActivity)
                .load(url)
                .placeholder(R.drawable.default_board_icon)
                .dontAnimate()
                .into(holder.icon)
            holder.itemView.tag = board
            holder.name.text = board.name

            if(mCategory.isBookmarkCategory){
                holder.itemView.setOnLongClickListener {
                    BoardModel.removeBookmark(board.fid,board.stid)
                    notifyDataSetChanged()
                    true
                }
            }
            RxUtils.clicks(holder.itemView) {
                BoardModel.addRecentBoard(board)
                RxBus.getInstance().post(RxEvent(RxEvent.EVENT_SHOW_TOPIC_LIST, board))
                notifyDataSetChanged()
            }


        } else {
            val subCategoryIndex = mTitlePositions.indexOf(position)
            val subCategory = mCategory.getSubCategory(subCategoryIndex)
            holder.name.text = subCategory.name
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mTitlePositions.contains(position)) TITLE_ITEM else BOARD_ITEM
    }

    override fun getItemCount(): Int {
        return mTotalCount
    }

    val layoutInflater: LayoutInflater
        get() = mActivity.layoutInflater

    companion object {
        const val BOARD_ITEM = 1
        const val TITLE_ITEM = 0
    }
}