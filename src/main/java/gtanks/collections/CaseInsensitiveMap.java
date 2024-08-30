/*
 * Decompiled with CFR 0.150.
 */
package gtanks.collections;

import gtanks.collections.strings.CaseInsensitiveString;
import java.util.HashMap;

public class CaseInsensitiveMap<T>
extends HashMap<CaseInsensitiveString, T> {

    @Override
    public T put(CaseInsensitiveString key, T obj) {
        return super.put(key, obj);
    }

    public T get(CaseInsensitiveString key) {
        return (T)super.get(key);
    }

    public T remove(CaseInsensitiveString key) {
        return (T)super.remove(key);
    }
}

