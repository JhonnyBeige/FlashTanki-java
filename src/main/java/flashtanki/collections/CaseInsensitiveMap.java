/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.collections;

import flashtanki.collections.strings.CaseInsensitiveString;
import java.util.HashMap;

public class CaseInsensitiveMap<T>
extends HashMap<CaseInsensitiveString, T> {

    @Override
    public T put(CaseInsensitiveString key, T obj) {
        return super.put(key, obj);
    }

    public T get(CaseInsensitiveString key) {
        return super.get(key);
    }

    public T remove(CaseInsensitiveString key) {
        return super.remove(key);
    }
}

