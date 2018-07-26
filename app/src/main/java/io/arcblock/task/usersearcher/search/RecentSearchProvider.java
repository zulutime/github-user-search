package io.arcblock.task.usersearcher.search;

import android.content.SearchRecentSuggestionsProvider;

public class RecentSearchProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "io.arcblock.task.recentsearchprovider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentSearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}