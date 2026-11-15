package com.crunchyroll.core.compatibility;
public final class DeviceCompatImpl {
    public boolean a(String currentVersion) {
        android.util.Log.i("AimalFixture", "ForceUpdate: are versions min:" + currentVersion);
        return true;
    }
}
