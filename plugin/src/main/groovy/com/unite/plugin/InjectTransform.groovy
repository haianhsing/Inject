package com.unite.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.transform.TransformException

class InjectTransform extends Transform {

    private Project mProject

    InjectTransform(Project project) {
        this.mProject = project
    }


    String getName() {
        return "InjectTransform"
    }


    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }


    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformInvocation.inputs.each { input ->
            // 包含我们手写的 Class 类及 R.class、BuildConfig.class 等
            input.directoryInputs.each { directoryInput ->
                String path = directoryInput.file.absolutePath
                println("[InjectTransform] Begin to inject: $path")

                // 执行注入逻辑
                InjectByJavassist.inject(path, mProject)

                // 获取输出目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                println("[InjectTransform] Directory output dest: $dest.absolutePath")

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            // jar文件，如第三方依赖
            input.jarInputs.each { jarInput ->
                def dest = transformInvocation.outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}