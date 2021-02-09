package me.m1dnightninja.midnightcore.api.permission;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String id;
    private final int priority;

    private final List<String> permissions = new ArrayList<>();

    public Group(String id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public void addPermission(String s) {
        if(permissions.contains(s)) return;
        permissions.add(s);
    }

    public void clearPermission(String s) {
        permissions.remove(s);
    }

    public Iterable<String> getPermissions() {
        return permissions;
    }

    public static Group parse(ConfigSection s) {

        String id = s.getString("id");
        int priority = s.getInt("priority");

        Group out = new Group(id, priority);

        if(s.has("permissions", List.class)) {
            for(Object o : s.getList("permissions")) {
                if(!(o instanceof String)) continue;
                out.permissions.add((String) o);
            }
        }

        return out;
    }
}
