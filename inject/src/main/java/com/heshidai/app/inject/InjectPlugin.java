package com.heshidai.app.inject;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class InjectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("========================");
        System.out.println("hello gradle plugin!");
        AppExtension android = project.getExtensions().findByType(AppExtension.class);
        if (android != null) {
            android.registerTransform(new ClazzTransform(project));
        } else {
            System.out.println("AppExtension is null~~~~");
        }
        System.out.println("========================");
    }
}
