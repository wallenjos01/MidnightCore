package org.wallentines.mcore;

import org.wallentines.mcore.lang.LocaleHolder;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;

public interface CommandSender extends PermissionHolder, LocaleHolder {

    void sendSuccess(Component component, boolean log);

    void sendFailure(Component component);

    Location getLocation();

}
