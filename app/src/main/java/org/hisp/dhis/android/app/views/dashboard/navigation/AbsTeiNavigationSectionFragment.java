package org.hisp.dhis.android.app.views.dashboard.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;

/**
 * Parent fragment for fragments within the dashboard navigation layout
 */

public abstract class AbsTeiNavigationSectionFragment extends BaseFragment {
    protected static final String ARG_ITEM_UID = "arg:itemUid";
//    private static final String ARG_CONTENT_ID = "arg:programUid";

    protected static final String ARG_ENROLLMENT_UID = "arg:enrollmentUid";
    protected static final String ARG_PROGRAM_UID = "arg:programUid";
    protected RecyclerView recyclerView;

    protected String getItemUid() {
        if (getArguments() == null || getArguments()
                .getString(ARG_ITEM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass item uid in intent extras");
        }

        return getArguments().getString(ARG_ITEM_UID, null);
    }

    protected String getProgramUid() {
        if (getArguments() == null || getArguments()
                .getString(ARG_PROGRAM_UID, null) == null) {
            throw new IllegalArgumentException("You must pass program uid in intent extras");
        }

        return getArguments().getString(ARG_PROGRAM_UID, null);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (getAdapter() instanceof ExpandableRecyclerAdapter) {
            ((ExpandableRecyclerAdapter) getAdapter()).onSaveInstanceState(savedInstanceState);
        }
    }

    protected abstract RecyclerView.Adapter getAdapter();

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (getAdapter() instanceof ExpandableRecyclerAdapter) {
            ((ExpandableRecyclerAdapter) getAdapter()).onRestoreInstanceState(savedInstanceState);
        }
    }

}
