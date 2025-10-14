plugins {
    kotlin("jvm") version "1.9.20"
    java
    application
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    testAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

tasks.register<JavaExec>("jcstress") {
    group = "verification"
    description = "Run JCStress stress tests"
    mainClass.set("org.openjdk.jcstress.Main")
    classpath = sourceSets.test.get().runtimeClasspath
    dependsOn("testClasses")

    val argsProp = project.findProperty("jcstressArgs") as String?
    if (!argsProp.isNullOrBlank()) {
        args = argsProp.split("\\s+".toRegex())
    }
}
