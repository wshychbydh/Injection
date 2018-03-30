package com.heshidai.app.inject;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by cool on 2018/3/30.
 */

public class ClazzTransform extends Transform {

    Project project;

    public ClazzTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return "pluginDex";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    //指明当前Transform是否支持增量编译
    @Override
    public boolean isIncremental() {
        return true;
    }


    @Override
    public void transform(Context context, Collection<TransformInput> inputs,
                          Collection<TransformInput> referencedInputs,
                          TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, InterruptedException {
        for (TransformInput it : inputs) {
            for (DirectoryInput directoryInput : it.getDirectoryInputs()) {
                InjectUtils.inject(directoryInput.getFile().getAbsolutePath(), project);
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
            for (JarInput jarInput : it.getJarInputs()) {
                String jarName = jarInput.getName();
                String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4);
                }
                File dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                FileUtils.copyFile(jarInput.getFile(), dest);
            }
        }
    }
}
