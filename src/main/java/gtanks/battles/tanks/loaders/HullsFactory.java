/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.tanks.loaders;

import gtanks.StringUtils;
import gtanks.battles.tanks.hulls.Hull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import gtanks.logger.LogType;
import gtanks.logger.LoggerService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HullsFactory {
    private static HullsFactory instance;
    private static LoggerService loggerService = LoggerService.getInstance();
    private HashMap<String, Hull> hulls = new HashMap();

    public static HullsFactory getInstance() {
        if (instance == null) {
            instance = new HullsFactory();
        }
        return instance;
    }

    public void init(String path2configs) {
        hulls.clear();
        try {
            File file = new File(path2configs);
            for (File config : file.listFiles()) {
                parse(config);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parse(File config) throws FileNotFoundException, IOException, ParseException {
        JSONObject jobj = (JSONObject) new JSONParser().parse(new FileReader(config));
        String type = (String) jobj.get("type");
        for (Object obj : (JSONArray) jobj.get("modifications")) {
            JSONObject jt = (JSONObject) obj;
            Hull hull = new Hull((float) ((Double) jt.get("mass")).doubleValue(), (float) ((Double) jt.get("power")).doubleValue(), (float) ((Double) jt.get("speed")).doubleValue(), (float) ((Double) jt.get("turn_speed")).doubleValue(), ((Long) jt.get("hp")).longValue());
            hulls.put(StringUtils.concatStrings(type, "_", (String) jt.get("modification")), hull);
        }
    }

    public Hull getHull(String id) {
        Hull hull = hulls.get(id);
        if (hull == null) {
            loggerService.log(LogType.ERROR, "Hull with id " + id + " is null!");
            return null;
        }
        return hull;
    }
}

