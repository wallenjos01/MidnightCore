package build.plugin;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.java.TargetJvmVersion;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.wallentines.gradle.mv.MultiVersionExtension;

public class MultiShadow {

    public static void setupShadow(Project project, MultiVersionExtension ext, int javaVersion, String classifier) {

        Configuration shadow = project.getConfigurations().getByName("shadow");

        Configuration shadowVer = project.getConfigurations().create("shadow" + javaVersion, cfg -> {
            cfg.extendsFrom(shadow);
            cfg.attributes(attr -> attr.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion));
        });

        Jar jar = ext.getJarTask(javaVersion);
        jar.getArchiveClassifier().set("partial-" + jar.getArchiveClassifier().get());

        TaskProvider<ShadowJar> shadowTask = project.getTasks().register("shadow" + javaVersion, ShadowJar.class, task -> {

            task.getArchiveClassifier().set(classifier);
            task.getConfigurations().clear();
            task.getConfigurations().add(shadowVer);

            ProcessResources processResources = project.getTasks().named("processResources", ProcessResources.class).get();
            task.dependsOn(processResources);

            task.from(ext.getSourceSet(javaVersion).getOutput());
            task.from(processResources.getDestinationDir());

        });

        project.getTasks().named("build", build -> {
            build.dependsOn(shadowTask);
        });

    }

}
