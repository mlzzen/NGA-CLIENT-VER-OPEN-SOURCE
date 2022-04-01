package nosc.ui.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class RecyclerViewFlipper<T extends RecyclerView.ViewHolder> extends ViewFlipper {

    private final ViewFlipperAdapterDataObserver mDataObserver = new ViewFlipperAdapterDataObserver();

    private RecyclerView.Adapter<T> mAdapter;

    public RecyclerViewFlipper(Context context) {
        super(context);
    }

    public RecyclerViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(RecyclerView.Adapter<T> adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerAdapterDataObserver(mDataObserver);
        updateView();
    }

    private void updateView() {
        removeAllViews();
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            T viewHolder = mAdapter.onCreateViewHolder(this, i);
            mAdapter.onBindViewHolder(viewHolder, i);
            addView(viewHolder.itemView);
        }
    }

    public RecyclerView.Adapter<T> getAdapter() {
        return mAdapter;
    }

    private class ViewFlipperAdapterDataObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            updateView();
        }
    }
}
