plugins {
    id("com.gradle.enterprise") version("3.6.1")
}
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
includeBuild("plugin-verifier")

