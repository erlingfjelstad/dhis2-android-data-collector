package org.hisp.dhis.android.app.views.drawerform.eventbus;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class DrawerFormBusImpl implements DrawerFormBus {

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    @Inject
    public DrawerFormBusImpl() {
        // NOOP, needed for Dagger 2 DI
    }

    @Override
    public void post(@NonNull Object event) {
        if (bus.hasObservers()) {
            bus.onNext(event);
        }
    }

    @Override
    public <T> Observable<T> observable(@NonNull final Class<T> eventClass) {

        return bus
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return o != null;
                    }
                }) // Filter out null objects, better safe than sorry
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object object) {
                        return eventClass.isInstance(object);
                    }
                }) // We're only interested in a specific event class
                .cast(eventClass); // Cast it for easier usage
    }
}
