package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IPermissionModule;
import me.m1dnightninja.midnightcore.api.permission.Group;

import java.util.*;

public class AbstractPermissionModule implements IPermissionModule {

    private final HashMap<UUID, List<String>> permissions = new HashMap<>();
    private final List<Group> groups = new ArrayList<>();

    protected ConfigSection section;

    @Override
    public boolean initialize(ConfigSection configuration) {

        if(section == null) return false;
        loadPermissions();

        return true;
    }

    @Override
    public String getId() {
        return "permission";
    }

    @Override
    public ConfigSection getDefaultConfig() {

        return null;
    }

    @Override
    public boolean hasPermission(UUID u, String permission) {

        if(!permissions.containsKey(u)) return false;

        Optional<Boolean> b = getPermissionState(getPermissions(u), permission);
        if(b.isPresent()) return b.get();

        if(!permission.startsWith("group.")) {
            for (Group g : getGroups(u)) {
                b = getPermissionState(g.getPermissions(), permission);
                if (b.isPresent()) return b.get();
            }
        }

        return false;
    }

    @Override
    public void grantPermission(UUID u, String permission) {
        permissions.putIfAbsent(u, new ArrayList<>());

        if(permissions.containsKey(u)) return;
        permissions.get(u).add(permission);
    }

    @Override
    public void clearPermission(UUID u, String permission) {

        if(!permissions.containsKey(u)) return;
        permissions.get(u).remove(permission);
    }

    @Override
    public Iterable<String> getPermissions(UUID u) {
        return permissions.containsKey(u) ? permissions.get(u) : new ArrayList<>();
    }

    @Override
    public Iterable<Group> getGroups(UUID u) {

        List<Group> out = new ArrayList<>();
        for(Group g : groups) {
            if(hasPermission(u, "group." + g.getId()) || g.getId().equals("default")) {
                out.add(g);
            }
        }
        if(out.isEmpty()) return out;

        out.sort(Comparator.comparingInt(Group::getPriority));
        return out;
    }

    @Override
    public boolean groupHasPermission(Group g, String permission) {
        return getPermissionState(g.getPermissions(), permission).orElse(false);
    }

    @Override
    public void grantGroupPermission(Group g, String permission) {
        g.addPermission(permission);
    }

    @Override
    public void clearGroupPermission(Group g, String permission) {
        g.clearPermission(permission);
    }

    @Override
    public Iterable<String> getGroupPermissions(Group g) {
        return g.getPermissions();
    }

    @Override
    public Group getGroup(String id) {
        for(Group g : groups) {
            if(g.getId().equals(id)) return g;
        }
        return null;
    }

    @Override
    public boolean registerGroup(Group g) {
        if(getGroup(g.getId()) != null) return false;

        groups.add(g);
        return true;
    }

    @Override
    public void reloadAllPermissions() {
        permissions.clear();
        groups.clear();

        loadPermissions();
        if(getGroup("default") == null) {
            registerGroup(new Group("default", 0));
        }
    }

    protected Optional<Boolean> getPermissionState(Iterable<String> perms, String permission) {

        for(String s : perms) {
            if(comparePermissions(s, permission)) return Optional.of(true);
            if(comparePermissions("-" + s, permission)) return Optional.of(false);
        }

        return Optional.empty();
    }

    protected boolean comparePermissions(String has, String needs) {

        if(has.equals(needs) || has.equals("*")) return true;

        String[] hasParts = has.split("\\.");
        String[] needsParts = needs.split("\\.");

        if(hasParts.length > needsParts.length) return false;

        for(int i = 0 ; i < hasParts.length ; i++) {

            if(hasParts[i].equals("*")) continue;
            if(!hasParts[i].equals(needsParts[i])) return false;

        }

        return true;
    }

    protected void loadPermissions() {

        if(section != null) {

            if(section.has("players", List.class)) {
                for(Object o : section.getList("players")) {
                    if(!(o instanceof ConfigSection)) continue;

                    ConfigSection sec = (ConfigSection) o;
                    if(!sec.has("id", String.class)) continue;

                    if(sec.has("permissions", List.class)) {
                        UUID uid = UUID.fromString(sec.getString("id"));
                        List<String> perms = new ArrayList<>();
                        for(Object ob : sec.getList("permissions")) {
                            if(!(ob instanceof String)) continue;
                            perms.add((String) ob);
                        }

                        MidnightCoreAPI.getLogger().info(uid.toString());
                        for(String s : perms) {
                            MidnightCoreAPI.getLogger().info(s);
                        }

                        permissions.put(uid, perms);
                    }
                }
            }

            if(section.has("groups", List.class)) {
                for(Object o : section.getList("groups")) {
                    if (!(o instanceof ConfigSection)) continue;
                    try {
                        groups.add(Group.parse((ConfigSection) o));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        MidnightCoreAPI.getLogger().warn("An exception occurred while trying to parse a group!");
                        ex.printStackTrace();
                    }
                }
            }
        }

        if(getGroup("default") == null) {
            registerGroup(new Group("default", 0));
        }
    }

}
