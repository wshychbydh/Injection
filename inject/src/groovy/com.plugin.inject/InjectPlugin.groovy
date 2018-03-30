package com.plugin.inject

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by hp on 2016/4/8.
 */
public class InjectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new ClazzTransform(project))
    }
}