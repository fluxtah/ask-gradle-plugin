package com.fluxtah.ask

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AskGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Apply necessary plugins
        project.plugins.apply("com.github.johnrengelman.shadow")

        // Register the custom task using Java API conventions
        val testPlugin: TaskProvider<Task> = project.tasks.register("testAskPlugin") {
            it.dependsOn("shadowJar")
        }

        // Configure the task
        testPlugin.configure {
            it.doLast {
                val shadowJar = project.tasks.findByName("shadowJar")
                val jarPath = shadowJar?.outputs?.files?.singleFile
                println("To test the plugin, run: 'ask --test-plugin ${jarPath?.absolutePath}'")
                println("Ensure your debugger is set to connect on port 5005 (or your configured port)")
            }
        }

        // Register deploy task
        val deployAskPlugin: TaskProvider<Task> = project.tasks.register("deployAskPlugin") {
            it.dependsOn("shadowJar")
        }

        // Configure the deploy task
        deployAskPlugin.configure {
            it.doLast {
                val shadowJar = project.tasks.findByName("shadowJar")
                val jarPath = shadowJar?.outputs?.files?.singleFile


                // copy the jar to the plugins directory in the .ask user directory
                if (jarPath != null) {
                    val askUserDir = System.getProperty("user.home") + "/.ask/plugins"
                    val askUserDirFile = File(askUserDir)
                    if (!askUserDirFile.exists()) {
                        askUserDirFile.mkdirs()
                    }
                    val pluginName = jarPath.nameWithoutExtension
                    val pluginFile = File(askUserDir, pluginName + ".jar")
                    Files.copy(jarPath.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    println("Plugin deployed to $pluginFile")
                }

            }
        }
    }
}
