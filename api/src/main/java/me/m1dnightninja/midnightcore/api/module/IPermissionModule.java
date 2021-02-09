package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.permission.Group;

import java.util.UUID;

public interface IPermissionModule extends IModule {

    boolean hasPermission(UUID u, String permission);

    void grantPermission(UUID u, String permission);

    void clearPermission(UUID u, String permission);

    Iterable<String> getPermissions(UUID u);

    Iterable<Group> getGroups(UUID u);

    boolean groupHasPermission(Group g, String permission);

    void grantGroupPermission(Group g, String permission);

    void clearGroupPermission(Group g, String permission);

    Iterable<String> getGroupPermissions(Group g);

    Group getGroup(String id);

    void reloadAllPermissions();

    boolean registerGroup(Group g);

}
