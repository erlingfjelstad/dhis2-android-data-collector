package org.hisp.dhis.android.app.views.drawerform.eventbus;

import android.support.annotation.NonNull;

import rx.Observable;

public interface DrawerFormBus {

    void post(@NonNull Object event);

    <T> Observable<T> observable(@NonNull Class<T> eventClass);
}
