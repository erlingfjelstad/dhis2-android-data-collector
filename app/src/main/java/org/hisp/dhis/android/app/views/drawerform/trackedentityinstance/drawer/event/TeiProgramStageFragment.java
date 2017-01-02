package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.event;

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
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.AbsTeiNavigationSectionFragment;
import org.hisp.dhis.client.sdk.ui.activities.ReportEntitySelection;
import org.hisp.dhis.client.sdk.ui.adapters.expandable.ExpandableAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.expandable.RecyclerViewSelection;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TeiProgramStageFragment extends AbsTeiNavigationSectionFragment implements TeiProgramStageView, RecyclerViewSelection {

    private static final String ARG_SELECTED_REPORT_ENTITY = "arg:selectedReportEntityUid";
    @Inject
    TeiProgramStagePresenter teiProgramStagePresenter;

    private ExpandableAdapter adapter;
    private ArrayList<ExpansionPanel> programStages;
    private String selectedReportEntityUid;
    private ReportEntitySelection reportEntitySelection;
    private Bundle savedInstanceState;

    public static TeiProgramStageFragment newInstance(String itemUid, String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ITEM_UID, itemUid);
        bundle.putString(ARG_PROGRAM_UID, programUid);

        TeiProgramStageFragment teiProgramStageFragment = new TeiProgramStageFragment();
        teiProgramStageFragment.setArguments(bundle);
        return teiProgramStageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        try {
            ((SkeletonApp) getActivity().getApplication()).getTeiDashboardComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }
        teiProgramStagePresenter.attachView(this);
        teiProgramStagePresenter.drawProgramStages(getItemUid(), getProgramUid());

        programStages = new ArrayList<>();
        adapter = new ExpandableAdapter(programStages);
        adapter.setRecyclerViewSelectionCallback(this);
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_tei_navigation, container, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            this.savedInstanceState = savedInstanceState;
            adapter.onRestoreInstanceState(savedInstanceState);
            selectedReportEntityUid = savedInstanceState.getString(ARG_SELECTED_REPORT_ENTITY);
            adapter.notifyDataSetChanged();
            reportEntitySelection.setSelectedUid(selectedReportEntityUid);
        }

        return recyclerView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        adapter.onSaveInstanceState(savedInstanceState);
        if (selectedReportEntityUid != null) {
            savedInstanceState.putString(ARG_SELECTED_REPORT_ENTITY, selectedReportEntityUid);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teiProgramStagePresenter.detachView();
    }

    @Override
    public void drawProgramStages(final List<ExpansionPanel> programStages) {
        adapter.swap(programStages);
        if (savedInstanceState != null) {
            adapter.onRestoreInstanceState(savedInstanceState);
        } else {
            // Expand all Program Stages by default
            adapter.expandAllParents();
        }
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

        teiProgramStagePresenter.showEventForm(uid);
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
