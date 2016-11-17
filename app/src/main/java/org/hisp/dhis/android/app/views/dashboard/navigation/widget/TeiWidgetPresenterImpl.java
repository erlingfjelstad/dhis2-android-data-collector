package org.hisp.dhis.android.app.views.dashboard.navigation.widget;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.ExpansionPanel;

import java.util.ArrayList;
import java.util.List;

public class TeiWidgetPresenterImpl implements TeiWidgetPresenter {

    private TeiWidgetsView teiWidgetsView;

    @Override
    public void attachView(View view) {
        teiWidgetsView = (TeiWidgetsView) view;
    }

    @Override
    public void detachView() {
        teiWidgetsView = null;
    }

    @Override
    public void drawWidgets(String enrollmentUid) {
        teiWidgetsView.drawWidgets(generateDummyData());
    }

    private List<ExpansionPanel> generateDummyData() {
        ArrayList<ExpansionPanel> widgets = new ArrayList<ExpansionPanel>();
        /* This test data crashes the app atm:
        ExpansionPanel indicatorsWidget = new ExpansionPanel("123", "Indicators", ExpansionPanel.Type.ACTION_NONE);
        ArrayList<ReportEntity> indicators = new ArrayList<ReportEntity>();
        indicators.add(new ReportEntity("123", ReportEntity.SyncStatus.SENT, null));
        indicators.add(new ReportEntity("124", ReportEntity.SyncStatus.SENT, null));
        indicators.add(new ReportEntity("125", ReportEntity.SyncStatus.SENT, null));
        indicatorsWidget.setChildren(indicators);
        widgets.add(indicatorsWidget);

        ExpansionPanel relationshipWidget = new ExpansionPanel("123", "Relationships", ExpansionPanel.Type.ACTION_ADD);
        ArrayList<ReportEntity> relationships = new ArrayList<ReportEntity>();
        relationships.add(new ReportEntity("123", ReportEntity.SyncStatus.SENT, null));
        relationships.add(new ReportEntity("124", ReportEntity.SyncStatus.SENT, null));
        relationships.add(new ReportEntity("125", ReportEntity.SyncStatus.SENT, null));
        relationshipWidget.setChildren(relationships);
        widgets.add(relationshipWidget);*/

        return widgets;
    }
}
