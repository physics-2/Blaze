package org.firstinspires.ftc.teamcode.blaze.blaze.MultiThread;


/**
 * Used to set how often is a function executed
 *
 * Always means that new function will launch without waiting for an previous one to stop
 * Non-overlaping means the same,but it will wait for an previous function to stop
 * Once means that you will manualy need to stop it for it to run again
*/

public enum ExecutionMode {
    ALWAYS,
    NON_OVERLAPPING,
    ONCE
}
