package org.hisp.dhis.android.app.views.dashboard.navigation.widget;

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
import org.hisp.dhis.client.sdk.ui.adapters.expandable.ExpandableAdapter;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by thomaslindsjorn on 13/10/16.
 */
public class TeiWidgetFragment extends AbsTeiNavigationSectionFragment implements TeiWidgetsView {

    @Inject
    TeiWidgetPresenter teiWidgetPresenter;
    ExpandableAdapter adapter;
    private ArrayList<ExpansionPanel> widgets;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initViews(inflater, container);

    /* TODO: Fix the injection stuff (?)
     try {
            ((SkeletonApp) getActivity().getApplication())
                    .getActivityComponent().inject(this);
        } catch (Exception e) {
            Log.e("DataEntryFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }

        teiWidgetPresenter.attachView(this);*/

        return recyclerView;
    }

    private void initViews(LayoutInflater inflater, @Nullable ViewGroup container) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_tei_navigation, container, false);
        widgets = new ArrayList<>();
        adapter = new ExpandableAdapter(widgets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teiWidgetPresenter.drawWidgets("enrollment Uid");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teiWidgetPresenter.detachView();
    }

    @Override
    public void drawWidgets(List<ExpansionPanel> widgets) {
        adapter.swap(widgets);
        adapter.expandAllParents();
    }

    @Override
    protected ExpandableRecyclerAdapter getAdapter() {
        return adapter;
    }
}
