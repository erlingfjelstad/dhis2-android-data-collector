package org.hisp.dhis.android.app.views.drawerform.form;

import org.hisp.dhis.android.app.views.drawerform.form.dataentry.DataEntryFragment;

public interface FormComponent {

    void inject(FormFragment formFragment);

    void inject(DataEntryFragment dataEntryFragment);
}
