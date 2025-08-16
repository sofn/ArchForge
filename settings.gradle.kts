rootProject.name = "MatrixBoot"

include("common-core")
include("common-error")
include("frame")
include("dependencies")

include("example:example-deploy")
include("example:example-task")
include("example:example-user")

// Configure build file names for subprojects
rootProject.children.forEach { project ->
    // All subprojects now use build.gradle.kts
    project.buildFileName = "build.gradle.kts"
    
    assert(project.projectDir.isDirectory)
    assert(project.buildFile.exists())
    assert(project.buildFile.isFile)
}
