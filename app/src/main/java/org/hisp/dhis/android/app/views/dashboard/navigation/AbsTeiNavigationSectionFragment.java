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


    protected static final String ARG_ENROLLMENT_UID = "arg:EnrollmentUid";
    protected RecyclerView recyclerView;

    protected String getEnrollmentUid() {
        if (getArguments().getString(ARG_ENROLLMENT_UID) == null) {
            throw new IllegalArgumentException("You must pass enrollment uid in intent extras");
        }

        return getArguments().getString(ARG_ENROLLMENT_UID);
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
