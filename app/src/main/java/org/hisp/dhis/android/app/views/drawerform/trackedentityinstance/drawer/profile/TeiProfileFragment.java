package org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.drawer.AbsTeiNavigationSectionFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;

import java.util.List;

import javax.inject.Inject;

public class TeiProfileFragment extends AbsTeiNavigationSectionFragment implements TeiProfileView {
    private static final String ARG_ITEM_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";

    @Inject
    TeiProfilePresenter teiProfilePresenter;

    private RowViewAdapter adapter;
    private FloatingActionButton floatingActionButton;

    public static TeiProfileFragment newInstance(String itemUid, String programUid) {
        TeiProfileFragment fragment = new TeiProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_UID, itemUid);
        args.putString(ARG_PROGRAM_UID, programUid);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

//        initViews(inflater, container);


        try {
            ((SkeletonApp) getActivity().getApplication())
                    .getTeiDashboardComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }

        teiProfilePresenter.attachView(this);


        return inflater.inflate(R.layout.fragment_tei_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_tei_profile);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RowViewAdapter(getFragmentManager());
        recyclerView.setAdapter(adapter);

        teiProfilePresenter.drawProfile(getItemUid(), getProgramUid());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teiProfilePresenter.detachView();
    }

    @Override
    public void update(List<FormEntity> formEntities) {
        adapter.swap(formEntities);
    }

    @Override
    public void toggleLockStatus() {
        adapter.toggleLockState();
    }


    //TODO: Review if we should still keep this in AbsTeiNavigationSectionFragment
//    private String getEventUid() {
//        if (getArguments() == null || getArguments()
//                .getString(ARG_ITEM_UID, null) == null) {
//            throw new IllegalArgumentException("You must pass item uid in intent extras");
//        }
//
//        return getArguments().getString(ARG_ITEM_UID, null);
//    }
//
//    private String getProgramUid() {
//        if (getArguments() == null || getArguments()
//                .getString(ARG_CONTENT_ID, null) == null) {
//            throw new IllegalArgumentException("You must pass program uid in intent extras");
//        }
//
//        return getArguments().getString(ARG_CONTENT_ID, null);
//    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return adapter;
    }


}
