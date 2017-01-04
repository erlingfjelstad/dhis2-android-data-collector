package org.hisp.dhis.android.app.views.drawerform.eventbus;

public class OnEventClickedEvent {

    private final String eventUid;

    public OnEventClickedEvent(String eventUid) {
        this.eventUid = eventUid;
    }

    public String getEventUid() {
        return eventUid;
    }
}
