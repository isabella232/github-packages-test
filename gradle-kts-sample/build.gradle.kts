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
    version = System.getenv()["GITHUB_TAG_NAME"]?.let {
        if (it.startsWith("v"))
            it.substring(1)
        else it
    } ?: "0.0.0"

    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}
