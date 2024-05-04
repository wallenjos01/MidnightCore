import build.plugin.Common

plugins {
    id("java")
    id("java-library")
    id("org.wallentines.gradle-multi-version")
    id("org.wallentines.gradle-patch")
}

multiVersion {
    defaultVersion(21)
    additionalVersions(17, 8)
}

patch {
    patchSet("java17", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(17))
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

multiVersion.getJarTask(8).archiveBaseName.set(Common.getArchiveName(project, rootProject))
multiVersion.getJarTask(17).archiveBaseName.set(Common.getArchiveName(project, rootProject))