plugins {
    `java-library`
}

description = "%NAME% business module"

dependencies {
    api("com.lesofn.archforge:archforge-common-jpa")
    api("com.lesofn.archforge:archforge-common-error")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
