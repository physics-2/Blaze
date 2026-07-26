package org.firstinspires.ftc.teamcode.blaze;

import com.qualcomm.robotcore.util.RobotLog;

public final class BlazeLogger {
    private BlazeLogger(){

    }

    static boolean enabled = true;
    static String defaultTag = "SYS";

    public static void disableLogs(){
        enabled = false;
    }

    public static void enableLogs(){
        enabled = true;
    }
    public static void addDefaultLog(String tag, String logMessage){
        RobotLog.d(deafultLogPreprocess(tag,logMessage));
    }

    public static void setDefaultTag(String defaultTag) {
        BlazeLogger.defaultTag = defaultTag;
    }

    public static void addWarnLog(String tag, String logMessage){
        RobotLog.w(deafultLogPreprocess(tag,logMessage));
    }

    public static void addErrorLog(String tag,String logMessage){
        RobotLog.e(deafultLogPreprocess(tag,logMessage));
    }


    private static String deafultLogPreprocess(String tag,String logMessage){
        String finalString;
        if(tag.equals("")){
            tag = defaultTag;
        }
        if(!enabled){
            finalString = "";
        }
        else{
            finalString = "[|[ BLAZE_" + tag + " : " + logMessage + " ]|]";
        }
        return finalString;
    }
}
