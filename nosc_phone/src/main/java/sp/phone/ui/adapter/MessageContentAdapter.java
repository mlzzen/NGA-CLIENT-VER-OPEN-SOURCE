package sp.phone.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.base.util.ToastUtils;
import gov.anzong.androidnga.databinding.ListMessageContentBinding;
import sp.phone.ui.adapter.beta.BaseAppendableAdapterEx;
import nosc.api.bean.MessageArticlePageInfo;
import nosc.api.bean.MessageDetailInfo;
import sp.phone.theme.ThemeManager;
import sp.phone.util.FunctionUtils;
import sp.phone.view.RecyclerViewEx;

/**
 * Created by Justwen on 2017/10/15.
 */

public class MessageContentAdapter extends BaseAppendableAdapterEx<MessageDetailInfo, MessageContentAdapter.MessageViewHolder> implements RecyclerViewEx.IAppendableAdapter {

    private List<MessageDetailInfo> mInfoList = new ArrayList<>();

    private boolean mPrompted;

    private int mTotalCount;

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView nickName;

        public TextView floor;

        public TextView postTime;

        public WebView content;

        public MessageViewHolder(ListMessageContentBinding binding) {
            super(binding.getRoot());
            nickName = binding.nickName;
            floor = binding.floor;
            postTime = binding.postTime;
            content = binding.content;
        }
    }

    public MessageContentAdapter(Context context) {
        super(context);
    }

    @Override
    protected void onBindItemViewHolder(MessageViewHolder holder, int position) {
        handleJsonList(holder, position);
        if (position + 1 == getItemCount()
                && !hasNextPage()
                && !mPrompted) {
            ToastUtils.info(R.string.last_page_prompt_message_detail);
            mPrompted = true;
        }
    }

    @Override
    protected MessageViewHolder onCreateItemViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new MessageViewHolder(ListMessageContentBinding.inflate(inflater, parent, false));
    }

    private MessageArticlePageInfo getEntry(int position) {
        return mInfoList.get(position / 20).getMessageEntryList().get(position % 20);
    }

    public void setData(MessageDetailInfo data) {
        if (data != null) {
            if (data.get__currentPage() == 1) {
                reset();
            }
            mInfoList.add(data);
            mTotalCount += data.getMessageEntryList().size();
            setNextPageEnabled(data.get__nextPage() > 0);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getNextPage() {
        return mInfoList.size() + 1;
    }

    private void reset() {
        mTotalCount = 0;
        mInfoList.clear();
        mPrompted = false;
        setNextPageEnabled(true);
    }

    private void handleJsonList(MessageViewHolder holder, int position) {
        final MessageArticlePageInfo entry = getEntry(position);
        if (entry == null) {
            return;
        }
        Resources res = mContext.getResources();
        ThemeManager theme = ThemeManager.getInstance();
        holder.postTime.setText(entry.getTime());
        String floor = String.valueOf(entry.getLou());
        holder.floor.setText("#" + floor);
        holder.nickName.setTextColor(res.getColor(theme.getForegroundColor()));
        holder.postTime.setTextColor(res.getColor(theme.getForegroundColor()));
        holder.floor.setTextColor(res.getColor(theme.getForegroundColor()));


        FunctionUtils.handleNickName(entry, res.getColor(theme.getForegroundColor()), holder.nickName, mContext);

        int colorId = theme.getBackgroundColor(position + 1);
        final int bgColor = res.getColor(colorId);
        int fgColorId = theme.getForegroundColor();
        final int fgColor = res.getColor(fgColorId);
        holder.itemView.setBackgroundResource(colorId);
        FunctionUtils.handleContentTV(holder.content, entry, bgColor, fgColor, mContext);

    }

    @Override
    protected int getItemViewCount() {
        return mTotalCount;
    }

}
