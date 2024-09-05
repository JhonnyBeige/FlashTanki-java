/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.anticheats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface AnticheatModel {
    public String name();

    public String actionInfo();
}

