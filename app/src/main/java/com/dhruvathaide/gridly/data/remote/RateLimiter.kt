package com.dhruvathaide.gridly.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

/**
 * Token Bucket Rate Limiter.
 * Limits:
 * - Max Burst (Capacity): 10 requests
 * - Refill Rate: 120 requests per minute = 2 requests per second (1 token every 500ms)
 */
object RateLimiter {
    private val mutex = Mutex()

    private const val MAX_TOKENS = 10.0
    private const val REFILL_RATE_PER_MS = 2.0 / 1000.0 // 2 tokens per second
    
    private var tokens = MAX_TOKENS
    private var lastRefillTimestamp = System.currentTimeMillis()

    suspend fun acquire() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val timePassed = now - lastRefillTimestamp
            
            // Refill tokens
            val tokensToAdd = timePassed * REFILL_RATE_PER_MS
            tokens = min(MAX_TOKENS, tokens + tokensToAdd)
            lastRefillTimestamp = now

            if (tokens >= 1.0) {
                // We have enough tokens, consume one immediately
                tokens -= 1.0
            } else {
                // Not enough tokens, calculate required wait time
                val missingTokens = 1.0 - tokens
                val waitTimeMs = (missingTokens / REFILL_RATE_PER_MS).toLong()
                
                if (waitTimeMs > 0) {
                    delay(waitTimeMs)
                }
                
                // After waiting, we virtually consumed the token that arrived
                // Update timestamp to 'now + waitTime' to reflect the consumption time
                // Or simply reset to 0 because we just spent the incoming token
                tokens = 0.0
                lastRefillTimestamp = System.currentTimeMillis()
            }
        }
    }
}
