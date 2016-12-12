package org.hisp.dhis.android.app.views.selectedcontent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.hisp.dhis.android.app.R;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SelectedContentActivity extends FragmentActivity {

    private static final String ARG_CONTENT_ID = "arg:contentId";
    private static final String ARG_CONTENT_TITLE = "arg:contentTitle";
    private static final String ARG_CONTENT_TYPE = "arg:contentType";

    public static void navigateTo(Activity activity, String itemId, String contentTitle, String contentType) {
        navigateToItem(activity, itemId, contentTitle, contentType);
    }

    private static void navigateToItem(Activity activity, String contentId, String contentTitle, String contentType) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, SelectedContentActivity.class);
        intent.putExtra(ARG_CONTENT_ID, contentId);
        intent.putExtra(ARG_CONTENT_TITLE, contentTitle);
        intent.putExtra(ARG_CONTENT_TYPE, contentType);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_content);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_selected_content,
                        SelectedContentFragment.newInstance(
                                getContentId(), getContentTitle(), getContentType()))
                .commit();
    }

    private String getContentId() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_CONTENT_ID, null) == null) {
            throw new IllegalArgumentException("You must pass content id in intent extras");
        }

        return getIntent().getExtras().getString(ARG_CONTENT_ID, null);
    }

    private String getContentTitle() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_CONTENT_TITLE, null) == null) {
            throw new IllegalArgumentException("You must pass content title in intent extras");
        }

        return getIntent().getExtras().getString(ARG_CONTENT_TITLE, null);
    }

    private String getContentType() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_CONTENT_TYPE, null) == null) {
            throw new IllegalArgumentException("You must pass content type in intent extras");
        }

        return getIntent().getExtras().getString(ARG_CONTENT_TYPE, null);
    }

}
