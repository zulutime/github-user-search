package io.arcblock.task.usersearcher.search;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.arcblock.task.usersearcher.R;
import io.arcblock.task.usersearcher.util.TextAnimator;

public class DemoSearch {

    private Context mContext;
    private TextView mPrompt;
    private SearchView mSearchView;

    public DemoSearch(Context context, TextView demoPrompt, SearchView searchView) {
        mContext = context;
        mPrompt = demoPrompt;
        mSearchView = searchView;
    }

    public void setup() {
        String samplePrompt = mContext.getResources().getString(R.string.sample_prompt);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(samplePrompt);
        builder.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View view) {
                                setupDemoTerms();
                            }
                        },
                0,
                builder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mPrompt.setText(builder);
    }

    private void setupDemoTerms() {
        String[] samples = mContext.getResources().getStringArray(R.array.sample_queries);

        final SpannableStringBuilder builder = new SpannableStringBuilder();

        int startPos = 0;
        for (final String sample : samples) {
            builder.append(sample);
            builder.append(System.lineSeparator());
            builder.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View view) {
                                    mSearchView.setQuery(sample, true);
                                }
                            },
                    startPos,
                    builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            startPos = builder.length();
        }

        TextAnimator.run(mPrompt, builder);
    }
}
