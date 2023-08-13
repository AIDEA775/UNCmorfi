plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    kotlin("android") version "1.6.21" apply false
    id("com.google.devtools.ksp") version "1.6.21-1.0.6" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}