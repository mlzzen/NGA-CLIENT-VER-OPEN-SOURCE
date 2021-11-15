package sp.phone.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import gov.anzong.androidnga.R
import sp.phone.mvp.model.BoardModel
import sp.phone.mvp.model.entity.BoardCategory
import sp.phone.ui.adapter.BoardCategoryAdapter

/**
 * 版块分页
 */
class BoardCategoryFragment : Fragment() {
    private var mListView: RecyclerView? = null
    private var mAdapter: BoardCategoryAdapter? = null
    private var mBoardCategory: BoardCategory? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mBoardCategory = requireArguments().getParcelable("category")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mListView = view.findViewById(R.id.list)
        mAdapter = BoardCategoryAdapter(requireActivity(), mBoardCategory!!)
        mListView?.adapter = mAdapter
        setLayoutManager()
        if (mBoardCategory!!.isBookmarkCategory) {
            val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    BoardModel
                        .swapBookmark(viewHolder.adapterPosition, target.adapterPosition)
                    mListView?.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val board = mBoardCategory!!.getBoard(viewHolder.adapterPosition)
                    BoardModel.removeBookmark(board.fid, board.stid)
                    mListView?.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }
            })
            touchHelper.attachToRecyclerView(mListView)
        }
        mListView?.adapter = mAdapter
    }

    fun setLayoutManager(){
        val layoutManager:GridLayoutManager = getDefaultLayoutManager(requireContext()) as GridLayoutManager
        layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mAdapter!!.getItemViewType(position) == BoardCategoryAdapter.TITLE_ITEM) layoutManager.spanCount else 1
            }
        }
        mListView?.layoutManager = layoutManager
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLayoutManager()
    }

    companion object {
        private val TAG = BoardCategoryFragment::class.java.simpleName
        fun newInstance(category: BoardCategory?): Fragment {
            val f: Fragment = BoardCategoryFragment()
            val args = Bundle()
            args.putParcelable("category", category)
            f.arguments = args
            return f
        }

        fun getDefaultLayoutManager(context: Context): LayoutManager {
            val dm = context.resources.displayMetrics
            val width = dm.widthPixels // 屏幕宽度（像素）
            val density = dm.density // 屏幕密度（0.75 / 1.0 / 1.5）
            // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
            val screenWidth = (width / density).toInt() // 屏幕宽度(dp)
            return GridLayoutManager(context,  (screenWidth / 110).coerceAtLeast(1))
        }
    }
}