package io.arcblock.task.usersearcher;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.List;

import io.arcblock.task.usersearcher.list.UserListAdapter;
import io.arcblock.task.usersearcher.list.UserListItem;
import io.arcblock.task.usersearcher.list.UserListScrollListener;
import io.arcblock.task.usersearcher.query.QueryUrlBuilder;
import io.arcblock.task.usersearcher.query.RequestHandler;
import io.arcblock.task.usersearcher.search.DemoSearch;
import io.arcblock.task.usersearcher.search.RecentSearchProvider;

public class ScrollingActivity extends AppCompatActivity {

    private static String TAG = ScrollingActivity.class.getName();

    public static final String SP_KEY_SORT_SELECTION = "spKeySortSelection";

    public static final int STATUS_CODE_NO_ERROR = 0;
    public static final int STATUS_CODE_NETWORKING = 1;
    public static final int STATUS_CODE_GENERAL = 2;

    private SearchView mUserQueryInput;
    private UserListAdapter mListAdapter;
    private RecyclerView mUserListView;
    private NestedScrollView mProgressSpinner;
    private NestedScrollView mStatusMessageContainer;
    private TextView mStatusMessageView;
    private TextView mDemoView;
    private SharedPreferences mSp;
    private RequestHandler mRequestHandler;
    private SearchRecentSuggestions mSuggestions;
    private DemoSearch mDemoSearch;

    public static final int MSG_START_SEARCH_NEXT = 101;
    public static final int MSG_STOP_SEARCH_NEXT = 102;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MSG_START_SEARCH_NEXT: {
                    mProgressSpinner.setVisibility(View.VISIBLE);
                    break;
                }
                case MSG_STOP_SEARCH_NEXT: {
                    doneSearchNext(msg.arg1);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.filter));

        mSp = PreferenceManager.getDefaultSharedPreferences(this);

        mProgressSpinner = findViewById(R.id.progress_spinner);
        mStatusMessageContainer = findViewById(R.id.status_scrollview);
        mStatusMessageView = mStatusMessageContainer.findViewById(R.id.status_message);
        mDemoView = mStatusMessageContainer.findViewById(R.id.sample_text);
        mDemoView.setMovementMethod(LinkMovementMethod.getInstance());

        mUserListView = findViewById(R.id.user_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mUserListView.setLayoutManager(layoutManager);
        mUserListView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        mListAdapter = new UserListAdapter(this);
        mUserListView.setAdapter(mListAdapter);
        mUserListView.addOnScrollListener(
                new UserListScrollListener(this, mListAdapter, layoutManager, mHandler));

        mRequestHandler = new RequestHandler(this, new RequestHandler.QueryCallback() {
            @Override
            public void onStartSearch() {
                startNewSearch();
            }

            @Override
            public void onValidResponse(List<UserListItem> list) {
                doneSearchNew(STATUS_CODE_NO_ERROR, list.size());
                mListAdapter.updateData(list);
                mUserListView.scheduleLayoutAnimation();
            }

            @Override
            public void onError(int errorCode) {
                doneSearchNew(errorCode, 0);
            }

            @Override
            public void onNextLink(String url) {
                mListAdapter.setNextUrl(url);
            }
        });

        mSuggestions = new SearchRecentSuggestions(this,
                RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);

        mUserQueryInput = findViewById(R.id.user_search_input);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mUserQueryInput.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mUserQueryInput.setFocusable(false);
        mUserQueryInput.setIconifiedByDefault(false);
        mUserQueryInput.clearFocus();
        mUserQueryInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // clear search result when search term is cleared
                if (newText == null || newText.isEmpty()) {
                    mListAdapter.updateData(null);
                    doneSearchNew(STATUS_CODE_NO_ERROR, 0);
                }

                return false;
            }
        });

        mDemoSearch = new DemoSearch(this, mDemoView, mUserQueryInput);
        doneSearchNew(STATUS_CODE_NO_ERROR, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mSuggestions.saveRecentQuery(query, null);
            mUserQueryInput.setQuery(query, false);
            mUserQueryInput.clearFocus();

            int sortOrderId = mSp.getInt(SP_KEY_SORT_SELECTION, 0);
            String url = QueryUrlBuilder.build(query, sortOrderId);
            mRequestHandler.runSearch(url);
        }
    }

    private void startNewSearch() {
        mListAdapter.updateData(null);

        mStatusMessageContainer.setVisibility(View.INVISIBLE);
        mProgressSpinner.setVisibility(View.VISIBLE);
    }

    private void doneSearchNew(int errorCode, int listCount) {
        mProgressSpinner.setVisibility(View.INVISIBLE);
        mDemoView.setVisibility(View.INVISIBLE);

        if (errorCode == STATUS_CODE_GENERAL) {
            mStatusMessageContainer.setVisibility(View.VISIBLE);
            mStatusMessageView.setText(R.string.status_error_general);
        } else if (errorCode == STATUS_CODE_NETWORKING) {
            mStatusMessageContainer.setVisibility(View.VISIBLE);
            mStatusMessageView.setText(R.string.status_error_network);
        } else {
            if (listCount == 0) {
                mStatusMessageContainer.setVisibility(View.VISIBLE);
                mStatusMessageView.setText(R.string.status_no_result);
                mDemoView.setVisibility(View.VISIBLE);
                mDemoSearch.setup();
            } else {
                mStatusMessageContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void doneSearchNext(int errorCode) {
        mProgressSpinner.setVisibility(View.INVISIBLE);

        // debounce the SnackBar showing. Only send another request when the SnackBar is dismissed
        Snackbar.Callback callback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                mListAdapter.setLoadable(true);
            }
        };

        if (errorCode == STATUS_CODE_GENERAL) {
            Snackbar.make(mStatusMessageContainer, R.string.status_error_general, Snackbar.LENGTH_LONG)
                    .addCallback(callback)
                    .show();
        } else if (errorCode == STATUS_CODE_NETWORKING) {
            Snackbar.make(mStatusMessageContainer, R.string.status_error_network, Snackbar.LENGTH_LONG)
                    .addCallback(callback)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); ++i) {
            menu.getItem(i).setChecked(false);
        }

        int selectedId = mSp.getInt(SP_KEY_SORT_SELECTION, 0);
        if (selectedId != 0) {
            menu.findItem(selectedId).setChecked(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void clearCache() {
        mSuggestions.clearHistory();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int currId = item.getItemId();
        if (currId == R.id.clear_cache) {
            clearCache();
            return super.onOptionsItemSelected(item);
        }

        int prevId = mSp.getInt(SP_KEY_SORT_SELECTION, 0);
        if (currId == prevId) {
            return super.onOptionsItemSelected(item);
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SharedPreferences.Editor editor = mSp.edit();
                editor.putInt(SP_KEY_SORT_SELECTION, item.getItemId());
                editor.commit();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mUserQueryInput.getQuery().length() > 0) {
                    Log.d(TAG, "resending request");
                    mUserQueryInput.setQuery(mUserQueryInput.getQuery(), true);
                }

                super.onPostExecute(aVoid);
            }
        }.execute();

        return super.onOptionsItemSelected(item);
    }
}
