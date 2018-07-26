package io.arcblock.task.usersearcher.list;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import io.arcblock.task.usersearcher.query.RequestHandler;

import static io.arcblock.task.usersearcher.ScrollingActivity.MSG_START_SEARCH_NEXT;
import static io.arcblock.task.usersearcher.ScrollingActivity.MSG_STOP_SEARCH_NEXT;
import static io.arcblock.task.usersearcher.ScrollingActivity.STATUS_CODE_NO_ERROR;

public class UserListScrollListener extends RecyclerView.OnScrollListener{

    private static final String TAG = UserListScrollListener.class.getSimpleName();
    private final LinearLayoutManager mLayoutManager;
    private final Handler mHandler;
    private final RequestHandler mRequestHandler;
    private final UserListAdapter mAdapter;

    public UserListScrollListener(Context context, UserListAdapter adapter,
                           LinearLayoutManager layoutManager, Handler handler) {
        mAdapter = adapter;
        mLayoutManager = layoutManager;
        mHandler = handler;

        mRequestHandler = new RequestHandler(context, new RequestHandler.QueryCallback() {
            @Override
            public void onStartSearch() {
                sendMessage(MSG_START_SEARCH_NEXT, 0, 0);
            }

            @Override
            public void onValidResponse(List<UserListItem> list) {
                sendMessage(MSG_STOP_SEARCH_NEXT, STATUS_CODE_NO_ERROR, list.size());
                if (!list.isEmpty()) {
                    int oldListSize = mAdapter.mList.size();
                    mAdapter.mList.addAll(list);
                    mAdapter.notifyItemRangeInserted(oldListSize, list.size());
                }
            }

            @Override
            public void onError(int errorCode) {
                sendMessage(MSG_STOP_SEARCH_NEXT, errorCode, 0);
            }

            @Override
            public void onNextLink(String url) {
                mAdapter.setNextUrl(url);
            }
        });
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy < 0) {
            return;
        }

        int firstVisibleIndex = mLayoutManager.findFirstVisibleItemPosition();
        int totalVisibleCount = mLayoutManager.getChildCount();
        int totalLoadedCount = mLayoutManager.getItemCount();

        if (mAdapter.canLoadNext() && firstVisibleIndex + totalVisibleCount >= totalLoadedCount) {
            mAdapter.setLoadable(false);
            Log.d(TAG, "loading next");

            mRequestHandler.runSearch(mAdapter.getNextUrl());
        }
    }

    private void sendMessage(int what, int arg1, int arg2) {
        Message msg = mHandler.obtainMessage(what, arg1, arg2);
        mHandler.sendMessage(msg);
    }
}
