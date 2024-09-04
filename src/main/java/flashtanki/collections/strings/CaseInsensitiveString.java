/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.collections.strings;

public class CaseInsensitiveString {
    private final String value;
    private final int hash;

    public String get() {
        return this.value;
    }

    public CaseInsensitiveString(String value) {
        this.value = value;
        String lc = value.toLowerCase();
        this.hash = lc.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CaseInsensitiveString that) {
            return this.value.equalsIgnoreCase(that.value);
        }
        return false;
    }

    public int hashCode() {
        return this.hash;
    }
}

