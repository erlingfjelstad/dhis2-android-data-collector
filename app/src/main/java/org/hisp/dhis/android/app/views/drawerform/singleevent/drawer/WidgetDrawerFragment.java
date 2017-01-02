package org.hisp.dhis.android.app.views.drawerform.singleevent.drawer;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.app.R;
import org.hisp.dhis.android.app.SkeletonApp;
import org.hisp.dhis.android.app.views.drawerform.RightDrawerController;

import javax.inject.Inject;

public class WidgetDrawerFragment extends Fragment implements WidgetDrawerView {

    private static final String ARG_EVENT_UID = "arg:itemUid";
    private static final String ARG_PROGRAM_UID = "arg:programUid";
    private static final String ARG_TWO_PANE_LAYOUT = "arg:twoPaneLayout";

    private String eventUid;
    private String programUid;

    @Inject
    WidgetDrawerPresenter widgetDrawerPresenter;

    @Inject
    RightDrawerController rightDrawerController;

    public WidgetDrawerFragment() {
        // Required empty public constructor
    }

    public static WidgetDrawerFragment newInstance(String param1, String param2, boolean useTwoPaneLayout) {
        WidgetDrawerFragment fragment = new WidgetDrawerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_UID, param1);
        args.putString(ARG_PROGRAM_UID, param2);
        args.putBoolean(ARG_TWO_PANE_LAYOUT, useTwoPaneLayout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ((SkeletonApp) getActivity().getApplication())
                    .getSingleEventDashboardComponent().inject(this);

            // attach view is called in this case from onCreate(),
            // in order to prevent unnecessary work which should be done
            // if case it will be i onResume()
            widgetDrawerPresenter.attachView(this);
        } catch (Exception e) {
            Log.e("WidgetDrawerFragment", "Activity or Application is null. Vital resources have been killed.", e);
        }

        if (getArguments() != null) {
            eventUid = getArguments().getString(ARG_EVENT_UID);
            programUid = getArguments().getString(ARG_PROGRAM_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_widget_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!useTwoPaneLayout()) {
            ((Toolbar) view.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_arrow_forward);
            ((Toolbar) view.findViewById(R.id.toolbar)).
                    setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rightDrawerController.hideMenu();
                        }
                    });

        }
    }

    private boolean useTwoPaneLayout() {
        return getArguments() != null && getArguments().getBoolean(ARG_TWO_PANE_LAYOUT);

    }
}
