package org.wallentines.mcore;

public interface PermissionHolder {

    /**
     * Checks if the holder has the given permission
     * @param permission The permission to check
     * @return Whether the holder has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Checks if the holder has the given permission or is at least the given operator level
     * @param permission The permission to check
     * @param defaultOpLevel The operator permission level
     * @return Whether the holder has the permission or the op level
     */
    boolean hasPermission(String permission, int defaultOpLevel);

    PermissionHolder ALL = new PermissionHolder() {
        @Override
        public boolean hasPermission(String permission) {
            return true;
        }
        @Override
        public boolean hasPermission(String permission, int defaultOpLevel) {
            return true;
        }
    };

    PermissionHolder NONE = new PermissionHolder() {
        @Override
        public boolean hasPermission(String permission) {
            return false;
        }
        @Override
        public boolean hasPermission(String permission, int defaultOpLevel) {
            return false;
        }
    };

}
