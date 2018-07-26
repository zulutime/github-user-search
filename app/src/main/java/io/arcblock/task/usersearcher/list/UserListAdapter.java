package io.arcblock.task.usersearcher.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.arcblock.task.usersearcher.query.ImageHandler;
import io.arcblock.task.usersearcher.R;

public class UserListAdapter extends RecyclerView.Adapter<UserListViewHolder> {

    private static final String TAG = UserListAdapter.class.getSimpleName();
    private final Context mContext;
    private final Bitmap mDefaultBitmap;

    private String mNextUrl;
    private boolean mCanLoadNext = true;

    List<UserListItem> mList = new ArrayList<>();

    public UserListAdapter(Context context) {
        mContext = context;
        mDefaultBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.account);
        mDefaultBitmap.setHasAlpha(true);
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new UserListViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, int position) {
        final UserListItem item = mList.get(position);

        holder.mAvatarView.setImageBitmap(mDefaultBitmap);
        if (item.avatar != null) {
            holder.mAvatarView.setImageBitmap(item.avatar);
        } else {
            ImageHandler.getAvatar(item.avatarUrl, new ImageHandler.ImageFetcherCallback() {
                @Override
                public void onImageAvailable(Bitmap bitmap) {
                    holder.mAvatarView.setImageBitmap(bitmap);
                    item.avatar = bitmap;
                }
            });
        }

        setupTextUI(holder.mUserLogin, item.login, null);
        setupTextUI(holder.mUserEmail, item.email, mContext.getResources().getString(R.string.user_email));
        setupTextUI(holder.mUserName, item.name, mContext.getResources().getString(R.string.user_fullname));
    }

    public void updateData(List<UserListItem> newList) {
        mList.clear();
        if (newList != null) {
            mList.addAll(newList);
        } else {
            mCanLoadNext = false;
            mNextUrl = null;
        }

        notifyDataSetChanged();
    }

    private void setupTextUI(TextView textView, String text, String prefix) {
        // clear recycled text
        textView.setText(null);
        if (text == null) {
            textView.setText(prefix);
            return;
        }

        if (prefix != null) {
            text = prefix + text;
        }

        textView.setText(text);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    boolean canLoadNext() {
        return mCanLoadNext;
    }

    public void setLoadable(boolean canLoadNext) {
        mCanLoadNext = canLoadNext;
    }

    public void setNextUrl(String url) {
        Log.d(TAG, "set loadable " + url);
        mNextUrl = url;
        mCanLoadNext = (url != null);
    }

    String getNextUrl() {
        return mNextUrl;
    }
}
