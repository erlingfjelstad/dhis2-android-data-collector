package org.hisp.dhis.android.app.views.dashboard.navigation.event;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.dashboard.navigation.AbsTeiNavigationSectionFragment;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;
import org.hisp.dhis.client.sdk.ui.adapters.expandable.ExpandableAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.expandable.RecyclerViewSelection;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TeiProgramStageFragment extends AbsTeiNavigationSectionFragment implements TeiProgramStageView, RecyclerViewSelection {

    @Inject
    TeiProgramStagePresenter teiProgramStagePresenter;

    private ExpandableAdapter adapter;
    private ArrayList<ExpansionPanel> programStages;
    private String selectedReportEntityUid = "";
    private ReportEntitySelection reportEntitySelection;

    public static TeiProgramStageFragment newInstance(String enrollmentUid) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ENROLLMENT_UID, enrollmentUid);

        TeiProgramStageFragment teiProgramStageFragment = new TeiProgramStageFragment();
        teiProgramStageFragment.setArguments(bundle);
        return teiProgramStageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        programStages = new ArrayList<>();
        adapter = new ExpandableAdapter(programStages);
        adapter.setRecyclerViewSelectionCallback(this);
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_tei_navigation, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
/*
//TODO: fix injection issues

        try {
            ((SkeletonApp) getActivity().getApplication()).getActivityComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }
*/

        teiProgramStagePresenter.attachView(this);
        return recyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teiProgramStagePresenter.drawProgramStages(getEnrollmentUid());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teiProgramStagePresenter.detachView();
    }

    @Override
    public void drawProgramStages(final List<ExpansionPanel> programStages) {
        adapter.swap(programStages);
    }

    @Override
    public void onEventClicked(FormEntity event) {
        teiProgramStagePresenter.onEventClicked(event.getId());
    }

    @Override
    protected ExpandableRecyclerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setSelectedUid(String uid) {
        selectedReportEntityUid = uid;
        adapter.notifyDataSetChanged();
        reportEntitySelection.setSelectedUid(uid);
    }

    @Override
    public String getSelectedUid() {
        return selectedReportEntityUid;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //same as super check instance vs interface & set it.
        if (context instanceof ReportEntitySelection) {
            this.reportEntitySelection = (ReportEntitySelection) context;
            //& init to whatever it says it should be:
            this.selectedReportEntityUid = reportEntitySelection.getSelectedUid();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //same as super, remove interface ref.
        this.reportEntitySelection = null;
    }
}
