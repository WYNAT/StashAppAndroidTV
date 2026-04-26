package com.github.damontecres.stashapp

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.net.HttpURLConnection
import java.net.URL

/**
 * A JUnit Rule that skips the test if the Stash live server is unreachable.
 */
class LiveTestRule(private val serverUrl: String = DEFAULT_URL) : TestRule {
    companion object {
        const val DEFAULT_URL = "http://192.168.189.44:9999"
        private var isReachable: Boolean? = null
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val url = System.getenv("STASH_LIVE_TEST_URL") ?: serverUrl
                if (!checkReachability(url)) {
                    println("Skipping live test: Server at $url is not reachable.")
                    return
                }
                base.evaluate()
            }
        }
    }

    private fun checkReachability(urlStr: String): Boolean {
        if (isReachable != null) return isReachable!!
        
        isReachable = try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            connection.responseCode == 200 || connection.responseCode == 401 // 401 is also okay as it means server is up
        } catch (e: Exception) {
            false
        }
        return isReachable!!
    }
}
