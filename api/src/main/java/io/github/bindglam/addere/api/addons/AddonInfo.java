package io.github.bindglam.addere.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface AddonInfo {
    String name();
    String version() default "NULL";
    String description() default "";
}
