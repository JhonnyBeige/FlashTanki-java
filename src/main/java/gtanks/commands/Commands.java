/*
 * Decompiled with CFR 0.150.
 */
package gtanks.commands;

import gtanks.commands.Command;
import gtanks.commands.Type;
import org.apache.commons.lang3.ArrayUtils;

public class Commands {
    public static final String SPLITTER_ARGS = ";";

    public static Command decrypt(String crypt) {
        Type type;
        String[] temp = crypt.split(SPLITTER_ARGS);
        switch (temp[0]) {
            case "auth": {
                type = Type.AUTH;
                break;
            }
            case "registration": {
                type = Type.REGISTRATON;
                break;
            }
            case "chat": {
                type = Type.CHAT;
                break;
            }
            case "lobby": {
                type = Type.LOBBY;
                break;
            }
            case "garage": {
                type = Type.GARAGE;
                break;
            }
            case "battle": {
                type = Type.BATTLE;
                break;
            }
            case "ping": {
                type = Type.PING;
                break;
            }
            case "lobby_chat": {
                type = Type.LOBBY_CHAT;
                break;
            }
            case "system": {
                type = Type.SYSTEM;
                break;
            }
            default: {
                type = Type.UNKNOWN;
            }
        }
        String[] args = ArrayUtils.removeElement(temp, temp[0]);
        return new Command(type, args);
    }
}

