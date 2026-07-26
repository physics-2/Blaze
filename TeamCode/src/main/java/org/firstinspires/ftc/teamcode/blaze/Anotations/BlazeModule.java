package org.firstinspires.ftc.teamcode.blaze.Anotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BlazeModule {
    String name() default "";
}