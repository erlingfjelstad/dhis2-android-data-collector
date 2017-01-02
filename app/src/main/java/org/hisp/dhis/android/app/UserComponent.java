/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.app;

import org.hisp.dhis.android.app.model.SyncAdapter;
import org.hisp.dhis.android.app.views.SelectorFragment;
import org.hisp.dhis.android.app.views.create.event.CreateEventActivity;
import org.hisp.dhis.android.app.views.create.identifiable.CreateIdentifiableItemActivity;
import org.hisp.dhis.android.app.views.dashboard.DashboardFragment;
import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardComponent;
import org.hisp.dhis.android.app.views.drawerform.singleevent.SingleEventDashboardModule;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardComponent;
import org.hisp.dhis.android.app.views.drawerform.trackedentityinstance.TeiDashboardModule;
import org.hisp.dhis.android.app.views.enrollment.EnrollmentComponent;
import org.hisp.dhis.android.app.views.enrollment.EnrollmentModule;
import org.hisp.dhis.android.app.views.selectedcontent.SelectedContentFragment;

import dagger.Subcomponent;

@PerUser
@Subcomponent(
        modules = {
                UserModule.class
        }
)
public interface UserComponent extends org.hisp.dhis.client.sdk.ui.bindings.commons.UserComponent {

    //------------------------------------------------------------------------
    // Sub-modules
    //------------------------------------------------------------------------

    TeiDashboardComponent plus(TeiDashboardModule teiDashboardModule);

    SingleEventDashboardComponent plus(SingleEventDashboardModule singleEventDashboardModule);

    EnrollmentComponent plus(EnrollmentModule enrollmentModule);

    //------------------------------------------------------------------------
    // Injection targets
    //------------------------------------------------------------------------

    void inject(SyncAdapter syncAdapter);

    void inject(SelectorFragment selectorFragment);

    void inject(DashboardFragment dashboardFragment);

    void inject(SelectedContentFragment selectedContentFragment);

    void inject(CreateIdentifiableItemActivity createIdentifiableItemActivity);

    void inject(CreateEventActivity createEventActivity);
}

