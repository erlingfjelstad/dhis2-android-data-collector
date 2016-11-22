package org.hisp.dhis.android.app.views.dashboard.navigation.profile;

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
import org.hisp.dhis.android.app.views.dashboard.navigation.AbsTeiNavigationSectionFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;

import java.util.List;

import javax.inject.Inject;

public class TeiProfileFragment extends AbsTeiNavigationSectionFragment implements TeiProfileView {

    @Inject
    TeiProfilePresenter teiProfilePresenter;
    private RowViewAdapter adapter;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_tei_profile, container, false);
//        initViews(inflater, container);


        try {
            ((SkeletonApp) getActivity().getApplication())
                    .getFormComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }

        teiProfilePresenter.attachView(this);


        return recyclerView;
    }

//    private void initViews(LayoutInflater inflater, @Nullable ViewGroup container) {
//        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_tei_navigation, container, false);
//
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(adapter);
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_tei_profile);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RowViewAdapter(getFragmentManager());
        recyclerView.setAdapter(adapter);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab_edit_profile);
        floatingActionButton.setVisibility(View.VISIBLE);

        teiProfilePresenter.drawProfile("Enrollment uid");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teiProfilePresenter.detachView();
    }

    @Override
    public void drawProfileItems(List<FormEntity> formEntities) {
        //adapter.swap(formEntities);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return adapter;
    }
}
