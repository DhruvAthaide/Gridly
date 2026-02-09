package com.dhruvathaide.gridly.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

/**
 * Token Bucket Rate Limiter.
 * Limits:
 * - Max Burst (Capacity): 5 requests
 * - Refill Rate: 30 requests per minute = 0.5 requests per second (1 token every 2000ms)
 */
object RateLimiter {
    private val mutex = Mutex()
    
    private const val MAX_TOKENS = 5.0
    private const val REFILL_RATE_PER_MS = 0.5 / 1000.0 // 0.5 tokens per second
    
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
