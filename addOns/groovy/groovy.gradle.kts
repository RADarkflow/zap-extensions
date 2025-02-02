import org.zaproxy.gradle.addon.AddOnStatus

description = "Adds Groovy support to ZAP"

zapAddOn {
    addOnName.set("Groovy Support")
    addOnStatus.set(AddOnStatus.BETA)
    zapVersion.set("2.11.0")

    manifest {
        author.set("ZAP Dev Team")
        url.set("https://www.zaproxy.org/docs/desktop/addons/groovy-support/")
    }
}

dependencies {
    implementation("org.codehaus.groovy:groovy-all:3.0.2")

    testImplementation(project(":testutils"))
    testImplementation(parent!!.childProjects.get("websocket")!!)
}
