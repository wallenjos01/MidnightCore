package org.wallentines.mcore;

import org.wallentines.mcore.lang.LocaleHolder;

public interface ConfiguringPlayer extends LocaleHolder, PermissionHolder {

    Server getServer();

}
