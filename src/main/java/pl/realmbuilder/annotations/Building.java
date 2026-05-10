package pl.realmbuilder.annotations;

import pl.realmbuilder.model.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)  // dostępna przez refleksję w czasie działania
@Target(ElementType.TYPE)            // nakładana na klasy
public @interface Building {
    String name();
    int cost();
    ResourceType produces();
    int amount();
    String description() default "";
}