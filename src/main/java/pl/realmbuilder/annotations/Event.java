package pl.realmbuilder.annotations;

import pl.realmbuilder.model.Season;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Event {
    String name();
    double probability();         // 0.0 - 1.0
    Season season() default Season.SPRING;  // domyślnie może wystąpić w każdej porze
    boolean anyseason() default false;      // true = występuje w każdej porze roku
}