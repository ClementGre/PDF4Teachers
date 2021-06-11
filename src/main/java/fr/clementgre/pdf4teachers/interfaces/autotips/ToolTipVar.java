package fr.clementgre.pdf4teachers.interfaces.autotips;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ToolTipVar{
    String actionKey();
    
    String prerequisiteKey();
    
    // "": Mouse location | "auto": currently focuses node
    String objectWhereDisplay();
}
