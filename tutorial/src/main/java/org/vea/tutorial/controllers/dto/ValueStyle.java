package org.vea.tutorial.controllers.dto;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(
        deepImmutablesDetection = true,
        visibility = Value.Style.ImplementationVisibility.PACKAGE,
        get = {"is*", "get*"})
@interface ValueStyle {
}
