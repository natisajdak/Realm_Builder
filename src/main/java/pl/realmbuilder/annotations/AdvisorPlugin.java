package pl.realmbuilder.annotations;

import pl.realmbuilder.model.Specialty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AdvisorPlugin {
    String name();
    Specialty specialty();
    int cost();                    // koszt użycia doradcy w złocie
    String description() default "";
}