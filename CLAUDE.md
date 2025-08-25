# bash command
- `./gradlew build`: build project.
- `./gradlew application:bootRun`: start SpringBoot server.  

# code style
- alibaba code style: https://github.com/alibaba/Alibaba-Java-Coding-Guidelines.
- all dependencies version defined in `dependencies/build.gradle.kts`.
- don't import `cn.hutool:hutool-all`.
- only use `jdk`, `apache-common`, `guava`, `spring` common tools.

# workflow
- After making a series of code changes, be sure to run a type check.
- For performance reasons, run a single test rather than the entire test suite.

# git
- don't auto commit 
