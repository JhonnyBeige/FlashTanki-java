/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users;

public enum TypeUser {
    DEFAULT{

        public String toString() {
            return "default";
        }
    }
    ,
    COMMUNITYMANAGER{

        public String toString() {
            return "communitymanager";
        }
    }
    ,
    HEADADMINISTRATOR{

        public String toString() {
            return "headadministrator";
        }
    }
    ,
    CHATADMINISTRATOR{

        public String toString() {
            return "chatadministrator";
        }
    }
    ,
    CHATMODERATOR{

        public String toString() {
            return "chatmoderator";
        }
    }
    ,
    CHATMODERATORCANDIDATE{

        public String toString() {
            return "chatmoderatorcandidate";
        }
    }
    ,
    BATTLEADMINISTRATOR{

        public String toString() {
            return "battleadministrator";
        }
    }
    ,
    BATTLEMODERATOR{

        public String toString() {
            return "battlemoderator";
        }
    }
    ,
    BATTLEMODERATORCANDIDATE{

        public String toString() {
            return "battlemoderatorcandidate";
        }
    }
    ,
    EVENTSADMINISTRATOR{

        public String toString() {
            return "eventsadministrator";
        }
    }
    ,
    EVENTSORGANIZER{

        public String toString() {
            return "eventsorganizer";
        }
    }
    ,
    SPECTATOR{

        public String toString() {
            return "spectator";
        }
    }
    ,
    TESTER{

        public String toString() {
            return "tester";
        }
    };
}
