package eu.europa.ec.euidi.verifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform