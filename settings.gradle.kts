rootProject.name = "AppBoot"

include("common:common-core")
include("common:common-error")
include("infrastructure")
include("dependencies")
include("server-admin")

include("domain:admin-user")

include("example:example-task")

// Configure build file names for subprojects
rootProject.children.forEach { project ->
    // All subprojects now use build.gradle.kts
    project.buildFileName = "build.gradle.kts"
    
    assert(project.projectDir.isDirectory)
    assert(project.buildFile.exists())
    assert(project.buildFile.isFile)
}
