package sp.phone.ui.adapter

import android.app.Activity
import sp.phone.mvp.model.entity.BoardCategory
import androidx.recyclerview.widget.RecyclerView
import sp.phone.ui.adapter.BoardCategoryAdapter.BoardViewHolder
import gov.anzong.androidnga.R
import android.widget.TextView
import android.view.ViewGroup
import sp.phone.mvp.model.entity.Board
import sp.phone.common.ApiConstants
import gov.anzong.androidnga.GlideApp
import sp.phone.rxjava.RxUtils
import sp.phone.rxjava.RxBus
import sp.phone.rxjava.RxEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import java.util.ArrayList

/**
 * 版块Grid Adapter
 */
class BoardCategoryAdapter(private val mActivity: Activity, private val mCategory: BoardCategory) :
    RecyclerView.Adapter<BoardViewHolder>() {
    private var mTitlePositions: MutableList<Int>  = ArrayList()
    private var mTotalCount = 0

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icon: ImageView? = itemView.findViewById(R.id.icon_board_img)

        var name: TextView? = itemView.findViewById(R.id.text_board_name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val layoutId =
            if (viewType == BOARD_ITEM) R.layout.list_board_item else R.layout.list_board_category_item
        val view = layoutInflater.inflate(layoutId, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        if (getItemViewType(position) == BOARD_ITEM) {
            val board: Board
            if (mTitlePositions.isNotEmpty()) {
                var realPosition = 0
                var subCategoryIndex = 0
                while (subCategoryIndex < mTitlePositions.size) {
                    if (mTitlePositions[subCategoryIndex] > position) {
                        break
                    }
                    subCategoryIndex++
                }
                subCategoryIndex--
                realPosition = position - mTitlePositions[subCategoryIndex] - 1
                val subCategory = mCategory.getSubCategory(subCategoryIndex)
                board = subCategory.getBoard(realPosition)
            } else {
                board = mCategory.getBoard(position)
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
                .into(holder.icon!!)
            holder.itemView.tag = board
            holder.name!!.text = board.name
            RxUtils.clicks(holder.itemView) {
                RxBus.getInstance().post(RxEvent(RxEvent.EVENT_SHOW_TOPIC_LIST, board))
            }
        } else {
            val subCategoryIndex = mTitlePositions.indexOf(position)
            val subCategory = mCategory.getSubCategory(subCategoryIndex)
            holder.name!!.text = subCategory.name
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mTitlePositions.contains(position)) TITLE_ITEM else BOARD_ITEM
    }

    override fun getItemCount(): Int {
        return mTotalCount
    }

    /*
        private int getResId(Board board) {
            if (board.getStid() != 0) {
                return 0;
            }
            int fid = board.getFid();
            String resName = fid > 0 ? "p" + fid : "p_" + Math.abs(fid);
            return mActivity.getResources().getIdentifier(resName, "drawable", mActivi.getPackageName());
        }

        private Drawable getDrawable(Board board) {
            Drawable drawable = null;
            int resId = getResId(board);
            if (resId != 0) {
                drawable = ContextCompat.getDrawable(mActivity, resId);
            }

            return drawable;
        }


     */
    val layoutInflater: LayoutInflater
        get() = mActivity.layoutInflater

    companion object {
        const val BOARD_ITEM = 1
        const val TITLE_ITEM = 0
    }

    init {
        if (mCategory.subCategoryList != null) {
            for (subCategory in mCategory.subCategoryList) {
                mTitlePositions.add(mTotalCount)
                mTotalCount += subCategory.boardList.size
                mTotalCount++
            }
        } else {
            mTotalCount = mCategory.boardList.size
        }
    }
}