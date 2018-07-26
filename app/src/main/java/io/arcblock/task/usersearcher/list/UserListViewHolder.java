package io.arcblock.task.usersearcher.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.arcblock.task.usersearcher.R;

public class UserListViewHolder extends RecyclerView.ViewHolder {

    ImageView mAvatarView;
    TextView mUserLogin;
    TextView mUserName;
    TextView mUserEmail;

    UserListViewHolder(View itemView) {
        super(itemView);

        mAvatarView = itemView.findViewById(R.id.user_avatar);
        mUserLogin = itemView.findViewById(R.id.user_login);
        mUserName = itemView.findViewById(R.id.user_full_name);
        mUserEmail = itemView.findViewById(R.id.user_email);
    }
}
