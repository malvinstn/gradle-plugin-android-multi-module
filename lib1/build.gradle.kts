plugins {
    id("com.android.library")
    id("my-plugin")
}

myOptions {
    jacoco {
        excludes(
            "**/api/entity/**",
            "**/*JsonAdapter*"
        )
    }
}
dependencies {
}
