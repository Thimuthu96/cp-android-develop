plugins {
    id("cloud.orelit.communicationplatform-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.features.${MODULE_NAME}.api"
}

dependencies {
    implementation(projects.libraries.architecture)
}
