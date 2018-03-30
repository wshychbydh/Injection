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

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by cool on 2018/3/30.
 */

public class ClazzTransform extends Transform {

    Project project;

    // 添加构造，为了方便从plugin中拿到project对象，待会有用
    public ClazzTransform(Project project) {
        this.project = project;
    }

    // Transfrom在Task列表中的名字
    // TransfromClassesWithPreDexForXXXX
    @Override
    public String getName() {
        return "pluginDex";
    }

    // 指定input的类型,需要处理的数据类型，有两种枚举类型
    //CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 指Transform要操作内容的范围，官方文档Scope有7种类型：
     * EXTERNAL_LIBRARIES        只有外部库
     * PROJECT                       只有项目内容
     * PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
     * PROVIDED_ONLY                 只提供本地或远程依赖项
     * SUB_PROJECTS              只有子项目。
     * SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * TESTED_CODE                   由当前变量(包括依赖项)
     */
    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    //指明当前Transform是否支持增量编译
    @Override
    public boolean isIncremental() {
        return false;
    }

    /**
     * Transform中的核心方法，
     * inputs中是传过来的输入流，其中有两种格式，一种是jar包格式，一种是目录格式。
     * outputProvider 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
     */
    @Override
    public void transform(Context context, Collection<TransformInput> inputs,
                          Collection<TransformInput> referencedInputs,
                          TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, InterruptedException {

        // inputs就是输入文件的集合
        // outputProvider可以获取outputs的路径

        // Transfrom的inputs有两种类型，一种是目录，一种是jar包，要分开遍历

        for (TransformInput it : inputs) {
            for (DirectoryInput directoryInput : it.getDirectoryInputs()) {
                //TODO 这里可以对input的文件做处理，比如代码注入！
                InjectUtils.inject(directoryInput.getFile().getAbsolutePath(), project);
                // 获取output目录
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }

            for (JarInput jarInput : it.getJarInputs()) {
                //TODO 这里可以对input的文件做处理，比如代码注入！
                //  InjectUtils.inject(jarInput.file.absolutePath, project)
                // 重命名输出文件（同目录copyFile会冲突）
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
