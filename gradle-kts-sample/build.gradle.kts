buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
    }
}

allprojects {
    group = "com.atlassian.ghtest"

    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}
