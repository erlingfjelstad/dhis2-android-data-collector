/*
 *  Copyright (c) 2016, University of Oslo
 *
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.app.sync;

import org.hisp.dhis.client.sdk.core.MetadataTask;
import org.hisp.dhis.client.sdk.core.event.EventInteractor;
import org.hisp.dhis.client.sdk.core.option.OptionSetInteractor;
import org.hisp.dhis.client.sdk.core.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.core.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.core.trackedentity.TrackedEntityInteractor;
import org.hisp.dhis.client.sdk.core.user.UserInteractor;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SyncDateWrapper;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;

public class SyncWrapper {

    private final SyncDateWrapper syncDateWrapper;

    // metadata
    private final OrganisationUnitInteractor organisationUnitInteractor;
    private final OptionSetInteractor optionSetInteractor;
    private final UserInteractor userInteractor;
    private final TrackedEntityInteractor trackedEntityInteractor;
    private final ProgramInteractor programInteractor;

    // data
    private final EventInteractor eventInteractor;

    public SyncWrapper(OrganisationUnitInteractor organisationUnitInteractor,
                       ProgramInteractor programInteractor,
                       EventInteractor eventInteractor,
                       SyncDateWrapper syncDateWrapper,
                       OptionSetInteractor optionSetInteractor,
                       UserInteractor userInteractor,
                       TrackedEntityInteractor trackedEntityInteractor) {
        this.organisationUnitInteractor = organisationUnitInteractor;
        this.programInteractor = programInteractor;
        this.eventInteractor = eventInteractor;
        this.syncDateWrapper = syncDateWrapper;
        this.optionSetInteractor = optionSetInteractor;
        this.userInteractor = userInteractor;
        this.trackedEntityInteractor = trackedEntityInteractor;
    }


    public Single<List<Program>> syncMetaData() throws IOException {
        final MetadataTask task = new MetadataTask(organisationUnitInteractor,
                userInteractor,
                programInteractor,
                optionSetInteractor,
                trackedEntityInteractor);

        return Single.create(new Single.OnSubscribe<List<Program>>() {
            @Override
            public void call(SingleSubscriber<? super List<Program>> singleSubscriber) {
                try {
                    task.sync();
                    singleSubscriber.onSuccess(programInteractor.store().queryAll());
                } catch (IOException e) {
                    singleSubscriber.onError(e);
                }
            }
        });

//        @Override
//            public void call(Subscriber<? super List<Program>> subscriber) {
//                try {
//                    task.sync();
//                    subscriber.onNext(programInteractor.store().queryAll());
//                } catch (IOException e) {
//                    subscriber.onError(e);
//                }
//            }


//        return Single.zip(Single.create(new Single.OnSubscribe<List<Program>>() {
//                                            @Override
//                                            public void call(SingleSubscriber<? super List<Program>> singleSubscriber) {
//                                                try {
//                                                    task.sync();
//                                                    singleSubscriber.onSuccess(programInteractor.store().queryAll());
//                                                } catch (IOException e) {
//                                                    singleSubscriber.onError(e);
//                                                }
//                                            }
//                                        }, new Func1<Object, List<Program>>() {
//                                            @Override
//                                            public List<Program> call(Object program) {
//                                                return null;
//                                            }
//                                        });
//        }), new Func1<>() {
//            @Override
//            public Object call(Object o) {
//                return null;
//            }
//        });


//                new Func1<List<Program>, List<Program>>() {
//            @Override
//            public List<Program> call(List<Program> programs) {
//                if (syncDateWrapper != null) {
//                    syncDateWrapper.setLastSyncedNow();
//                }
//                return programInteractor.store().queryAll();
//            }
//        }));


//        Set<ProgramType> programTypes = new HashSet<>();
//        programTypes.add(ProgramType.WITHOUT_REGISTRATION);
//
//        return Observable.zip(Observable.create(new Observable.OnSubscribe<List<OrganisationUnit>>() {
//                    @Override
//                    public void call(Subscriber<? super List<OrganisationUnit>> subscriber) {
//                        try {
//                            List<OrganisationUnit> orgUnits = organisationUnitInteractor.api().list(null).execute().body().items();
//                            organisationUnitInteractor.store().save(orgUnits);
//                            subscriber.onNext(orgUnits);
//                        } catch (IOException e) {
//                            subscriber.onError(e);
//                        } finally {
//                            subscriber.onCompleted();
//                        }
//                    }
//                }),
//                Observable.create(new Observable.OnSubscribe<List<Program>>() {
//                    @Override
//                    public void call(Subscriber<? super List<Program>> subscriber) {
//                        try {
//                            List<Program> programs = programInteractor.api().list(null).execute().body().items();
//                            programInteractor.store().save(programs);
//                            subscriber.onNext(programs);
//                        } catch (IOException e) {
//                            subscriber.onError(e);
//                        } finally {
//                            subscriber.onCompleted();
//                        }
//
//                    }
//                }),
//                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
//                    @Override
//                    public List<Program> call(List<OrganisationUnit> units, List<Program> programs) {
//                        if (syncDateWrapper != null) {
//                            syncDateWrapper.setLastSyncedNow();
//                        }
//                        return programs;
//                    }
//                });
    }

    public Observable<List<Event>> syncData() {
        return null;
//        return eventInteractor.list()
//                .switchMap(new Func1<List<Event>, Observable<List<Event>>>() {
//                    @Override
//                    public Observable<List<Event>> call(List<Event> events) {
//                        Set<String> uids = ModelUtils.toUidSet(events);
//                        if (uids != null && !uids.isEmpty()) {
//                            if (syncDateWrapper != null) {
//                                syncDateWrapper.setLastSyncedNow();
//                            }
//                            return eventInteractor.sync(uids);
//                        }
//                        return Observable.empty();
//                    }
//                });
    }

    public Observable<Boolean> checkIfSyncIsNeeded() {

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });

     /*       );

            if(eventInteractor==null)

            {
                // no eventInteractor exists - return false (i.e. sync is not needed)
                return null;
//            return Observable.create(new DefaultOnSubscribe<Boolean>() {
//                @Override
//                public Boolean call() {
//                    return false;
//                }
//            });
            }

            List<State> updateActions = new ArrayList<>();
            updateActions.add(State.TO_POST);
            updateActions.add(State.TO_UPDATE);

            return null;*/
//        return Observable.just(eventInteractor.store().query(updateActions))
//                .switchMap(new Func1<List<Event>, Observable<Boolean>>() {
//                    @Override
//                    public Observable<Boolean> call(final List<Event> events) {
//                        return Observable.create(new DefaultOnSubscribe<Boolean>() {
//                            @Override
//                            public Boolean call() {
//                                return events != null && !events.isEmpty();
//                            }
//                        });
//                    }
//                });
    }

//    public Observable<List<Event>> backgroundSync() throws IOException {
//        return syncMetaData()
//                .subscribeOn(Schedulers.io())
//                .switchMap(new Func1<List<Program>, Observable<List<Event>>>() {
//                    @Override
//                    public Observable<List<Event>> call(List<Program> programs) {
//                        if (programs != null) {
//                            return syncData();
//                        }
//
//                        return Observable.empty();
//                    }
//                });
//    }
}
