package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;


/**
 * This enum is used to describe which pool type to use
 *
 * Cashed - automaticly sets the amount of threads
 * Fixed - has a limited amount of thread(Real cpu cores,4 on each hub)
 * Single - has a single thread,use for background tasks
 * Scheduled - does a function every periodically
 */
public enum PoolType {
    CACHED,
    FIXED,
    SINGLE,
    SCHEDULED
}