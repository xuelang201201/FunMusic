package com.charles.funmusic.premission;

public interface PermissionListener {
    /**
     * Gets called each time we run Permission.permissionCompare() and some Permission is revoke/granted to us
     */
    void permissionsChanged(String permissionChanged);

    /**
     * Gets called each time we run Permission.permissionCompare() and some Permission is granted
     */
    void permissionsGranted(String permissionGranted);

    /**
     * Gets called each time we run Permission.permissionCompare() and some Permission is removed
     */
    void permissionsRemoved(String permissionRemoved);
}