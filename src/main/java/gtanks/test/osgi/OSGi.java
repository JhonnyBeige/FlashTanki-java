/*
 * Decompiled with CFR 0.150.
 */
package gtanks.test.osgi;

import java.util.HashMap;

public class OSGi {
    private static HashMap<Class<?>, Object> models = new HashMap();

    public static void registerModel(Object model, Class<?> _interface) {
        models.put(_interface, model);
    }

    public static void registerModel(Object model) {
        models.put(model.getClass(), model);
    }

    public static Object getModelByInterface(Class<?> _interface) {
        return models.get(_interface);
    }
}

