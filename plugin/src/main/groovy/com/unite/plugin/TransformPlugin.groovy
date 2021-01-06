package com.unite.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class TransformPlugin implements Plugin<Project> {

    void apply(Project target) {
        target.android.registerTransform(new InjectTransform(target))
    }
}
