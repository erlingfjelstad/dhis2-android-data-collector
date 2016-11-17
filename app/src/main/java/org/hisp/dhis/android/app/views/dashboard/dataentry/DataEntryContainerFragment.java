package org.hisp.dhis.android.app.views.dashboard.dataentry;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.views.dashboard.TeiDashboardPresenter;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataEntryContainerFragment extends Fragment {

    @Inject
    TeiDashboardPresenter teiDashboardPresenter;

    public DataEntryContainerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_data_entry_container, container, false);
    }

}
