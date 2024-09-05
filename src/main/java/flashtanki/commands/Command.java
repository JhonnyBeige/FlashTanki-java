/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.commands;

import flashtanki.utils.StringUtils;

public class Command {
    public Type type;
    public String[] args;

    public Command(Type type, String[] args) {
        this.type = type;
        this.args = args;
    }

    public String toString() {
        String argsString = StringUtils.concatStrings(this.args);
        return String.valueOf(this.type.toString()) + " " + argsString;
    }
}

