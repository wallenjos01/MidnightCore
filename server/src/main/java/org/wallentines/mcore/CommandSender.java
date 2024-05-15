package org.wallentines.mcore;

import org.wallentines.mcore.lang.LocaleHolder;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;

public interface CommandSender extends PermissionHolder, LocaleHolder {

    void sendSuccess(Component component, boolean log);

    default void sendSuccess(UnresolvedComponent component, boolean log) {
        sendSuccess(component.resolveFor(this), log);
    }

    void sendFailure(Component component);

    default void sendFailure(UnresolvedComponent component) {
        sendFailure(component.resolveFor(this));
    }

    Location getLocation();

}
