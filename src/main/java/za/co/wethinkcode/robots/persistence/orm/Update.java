package za.co.wethinkcode.robots.persistence.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mimic EoDSQL @Update annotation.
 * Marks interface methods that execute INSERT/UPDATE/DELETE queries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Update {
    String value();
}
