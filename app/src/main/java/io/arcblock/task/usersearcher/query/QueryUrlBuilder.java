package io.arcblock.task.usersearcher.query;

import android.net.Uri;
import android.util.Log;

import io.arcblock.task.usersearcher.R;

public class QueryUrlBuilder {

    private static final String TAG = QueryUrlBuilder.class.getSimpleName();

    private static final String GITHUB_USER_QUERY_URL = "https://api.github.com/search/users?";

    private static final String REQUEST_QUERY_KEY = "q";
    private static final String REQUEST_SORT_KEY = "sort";
    private static final String REQUEST_SORT_FOLLOWERS = "followers";
    private static final String REQUEST_SORT_REPOS = "repositories";
    private static final String REQUEST_SORT_JOINED = "joined";

    private static final String REQUEST_ORDER_KEY = "order";
    private static final String REQUEST_ORDER_DESC = "desc";
    private static final String REQUEST_ORDER_ASC = "asc";

    public static String build(String query, int sortOrderId) {
        Uri.Builder builder = Uri.parse(GITHUB_USER_QUERY_URL)
                .buildUpon()
                .appendQueryParameter(REQUEST_QUERY_KEY, query);

        switch (sortOrderId) {
            case R.id.action_followers_asc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_FOLLOWERS)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_ASC);
                break;
            }
            case R.id.action_followers_desc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_FOLLOWERS)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_DESC);
                break;
            }
            case R.id.action_repo_asc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_REPOS)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_ASC);
                break;
            }
            case R.id.action_repo_desc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_REPOS)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_DESC);
                break;
            }
            case R.id.action_joined_asc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_JOINED)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_ASC);
                break;
            }
            case R.id.action_joined_desc: {
                builder.appendQueryParameter(REQUEST_SORT_KEY, REQUEST_SORT_JOINED)
                        .appendQueryParameter(REQUEST_ORDER_KEY, REQUEST_ORDER_DESC);
                break;
            }
            default:
        }

        String url = builder.build().toString()
                .replace("%3A", ":").replace("%2B", "+");

        Log.d(TAG, "query " + url);
        return url;
    }
}
