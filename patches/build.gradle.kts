group = "app.sinistersike.morphe-patches"

patches {
    // TODO: Update this section with your project details.
    about {
        name = "UserXYZ Patches"
        description = "Patches for apps I like"
        source = "https://github.com/sinisterSike/morphe-patches.git"
        author = "sinisterSike"
        contact = "https://github.com/sinisterSike"
        website = "https://morphe.software/add-source?github=rushiranpise/morphe-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
