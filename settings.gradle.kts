plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "kiwi-event"
include("kiwi-event-core")
include("kiwi-event-coroutines")
include("kiwi-event-example")