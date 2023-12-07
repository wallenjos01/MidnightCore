package org.wallentines.mcore.util;

import net.fabricmc.loader.api.FabricLoader;

public class MappingUtil {

    public static void printIntermediary(Class<?> clazz) {

         System.out.println(clazz.getName() + " -> " + FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", clazz.getName()));
    }

}
