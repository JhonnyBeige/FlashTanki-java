/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.configurator.server.configuration;

import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.configurator.osgi.OSGi;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConfigurationsLoader {
    private static final String DEFAULT_PATH = "configurations/runner/";
    private static final String FORMAT_CONFIG = ".cfg";
    private static final String PARSER_CLASS_NAME = "class_name";
    private static final String PARSER_PARAMS_ARRAY = "params";
    private static final String PARSER_VALUE = "value";
    private static final String PARSER_VAR_NAME = "var";
    private static final JSONParser jsonParser = new JSONParser();
    private static final LoggerService loggerService = LoggerService.getInstance();

    public static void load(String pathToAllConfigs) {
        if (pathToAllConfigs == null || pathToAllConfigs.isEmpty()) {
            pathToAllConfigs = DEFAULT_PATH;
            loggerService.log(LogType.WARNING,"WARNING! Path to all configs is null! Use default: configurations/runner/");
        }
        File path = new File(pathToAllConfigs);
        for (File file : path.listFiles()) {
            if (!file.getPath().endsWith(FORMAT_CONFIG)) continue;
            ConfigurationsLoader.parseAndLoadClass(file);
        }
    }

    private static void parseAndLoadClass(File config) {
        try {
            JSONObject json = (JSONObject)jsonParser.parse(new FileReader(config));
            String className = (String)json.get(PARSER_CLASS_NAME);
            JSONArray params = (JSONArray)json.get(PARSER_PARAMS_ARRAY);
            Class<?> clazz = Class.forName(className);
            Object entity = clazz.newInstance();
            for (Object param : params) {
                JSONObject _param = (JSONObject)param;
                Field field = clazz.getDeclaredField((String)_param.get(PARSER_VAR_NAME));
                field.setAccessible(true);
                Param conf_param = new Param();
                conf_param.paramClassName = (String)_param.get(PARSER_CLASS_NAME);
                conf_param.value = _param.get(PARSER_VALUE);
                field.set(entity, conf_param.getValue());
            }
            OSGi.registerModel(entity);
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    static class Param {
        public String paramClassName;
        public Object value;

        Param() {
        }

        public Object getValue() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
            Object returnValue = null;
            switch (this.paramClassName) {
                case "java.lang.Integer": {
                    if (this.value instanceof Long) {
                        returnValue = (int)((Long)this.value).longValue();
                        break;
                    }
                    returnValue = (int)((Integer)this.value);
                    break;
                }
                case "java.lang.String": {
                    returnValue = (String)this.value;
                    break;
                }
                case "java.lang.Long": {
                    returnValue = (long)((Long)this.value);
                    break;
                }
                case "java.lang.Double": {
                    returnValue = (double)((Double)this.value);
                    break;
                }
                case "java.lang.Float": {
                    returnValue = Float.valueOf(((Float)this.value).floatValue());
                    break;
                }
                case "java.lang.Byte": {
                    returnValue = (byte)((Byte)this.value);
                    break;
                }
                default: {
                    loggerService.log(LogType.WARNING,"Dont primitive type! " + this.paramClassName);
                }
            }
            return returnValue;
        }
    }
}

