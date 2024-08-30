/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  javax.xml.bind.JAXBException
 */
package flashtanki.battles.maps;

import flashtanki.battles.maps.parser.Parser;
import flashtanki.battles.maps.parser.map.bonus.BonusRegion;
import flashtanki.battles.maps.parser.map.bonus.BonusType;
import flashtanki.battles.maps.parser.map.spawn.SpawnPosition;
import flashtanki.battles.maps.parser.map.spawn.SpawnPositionType;
import flashtanki.battles.maps.themes.MapThemeFactory;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.logger.LogType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.JAXBException;

import flashtanki.logger.LoggerService;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MapsLoaderService {
    private static final LoggerService loggerService = LoggerService.getInstance();
    public static HashMap<String, Map> maps = new HashMap();
    private static ArrayList<IMapConfigItem> configItems = new ArrayList();
    private static Parser parser;

    public static void initFactoryMaps() {
        loggerService.log(LogType.INFO,"Maps Loader Factory inited. Loading maps...");
        try {
            parser = new Parser();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        MapsLoaderService.loadConfig();
    }

    private static void loadConfig() {
        try {
            JSONParser mapsParser = new JSONParser();
            Object items = mapsParser.parse(new FileReader(new File("maps/config.json")));
            JSONObject obj = (JSONObject) items;
            JSONArray jarray = (JSONArray) obj.get("maps");
            for (Object objItem : jarray) {
                IMapConfigItem __item;
                JSONObject item = (JSONObject) objItem;
                String id = (String) item.get("id");
                String name = (String) item.get("name");
                String skyboxId = (String) item.get("skybox_id");
                Object ambientSoundId = item.get("ambient_sound_id");
                Object gameModeId = item.get("gamemode_id");
                long minRankLong = (Long) item.get("min_rank");
                int minRank = Integer.parseInt(String.valueOf(minRankLong));
                long maxRankLong = (Long) item.get("max_rank");
                int maxRank = Integer.parseInt(String.valueOf(maxRankLong));
                long maxPlayersLong = (Long) item.get("max_players");
                int maxPlayers = Integer.parseInt(String.valueOf(maxPlayersLong));
                boolean tdm = (Boolean) item.get("tdm");
                boolean ctf = (Boolean) item.get("ctf");
                boolean dom = (Boolean) item.get("dom");
                Object themeId = item.get("theme_id");
                __item = ambientSoundId == null || gameModeId == null ? new IMapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf, dom) : new IMapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf, dom, (String) ambientSoundId, (String) gameModeId);
                if (themeId != null) {
                    __item.themeName = (String) themeId;
                }
                configItems.add(__item);
            }
            MapsLoaderService.parseMaps();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseMaps() {
        File[] maps;
        File[] arrfile = maps = new File("maps").listFiles();
        int n = maps.length;
        for (int i = 0; i < n; ++i) {
            File file = arrfile[i];
            if (file.isDirectory() || !file.getName().endsWith(".xml")) continue;
            MapsLoaderService.parse(file);
        }
        loggerService.log(LogType.INFO,"Loaded all maps!\n");
    }

    @SneakyThrows
    private static void parse(File file) {
        loggerService.log(LogType.INFO,"Loading " + file.getName() + "...");
        IMapConfigItem temp = MapsLoaderService.getMapItem(file.getName().substring(0, file.getName().length() - 4));
        if (temp == null) {
            return;
        }

        Map map = new Map(
                temp.name,
                temp.id,
                temp.skyboxId,
                temp.themeName,
                temp.ambientSoundId == null || temp.gameMode == null ? MapThemeFactory.getDefaultMapTheme() : MapThemeFactory.getMapTheme(temp.ambientSoundId, temp.gameMode),
                temp.minRank,
                temp.maxRank,
                temp.maxPlayers,
                temp.tdm,
                temp.ctf,
                temp.dom,
                DigestUtils.md5Hex(new FileInputStream(file))
        );

        flashtanki.battles.maps.parser.map.Map parsedMap = null;
        try {
            parsedMap = parser.parseMap(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        for (SpawnPosition sp : parsedMap.getSpawnPositions()) {
            if (sp.getSpawnPositionType() == SpawnPositionType.NONE) {
                map.spawnPositonsDM.add(sp.getVector3());
            }
            if (sp.getSpawnPositionType() == SpawnPositionType.RED) {
                map.spawnPositonsRed.add(sp.getVector3());
            }
            if (sp.getSpawnPositionType() != SpawnPositionType.BLUE) continue;
            map.spawnPositonsBlue.add(sp.getVector3());
        }
        if (parsedMap.getBonusesRegion() != null) {
            for (BonusRegion br : parsedMap.getBonusesRegion()) {
                for (BonusType type : br.getType()) {
                    if (type == BonusType.CRYSTALL) {
                        map.crystallsRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type == BonusType.CRYSTALL_100) {
                        map.goldsRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type == BonusType.CRYSTALL) {
                        map.crystallsRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type == BonusType.ARMOR) {
                        map.armorsRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type == BonusType.DAMAGE) {
                        map.damagesRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type == BonusType.HEAL) {
                        map.healthsRegions.add(br.toServerBonusRegion());
                        continue;
                    }
                    if (type != BonusType.NITRO) continue;
                    map.nitrosRegions.add(br.toServerBonusRegion());
                }
            }
        }
        map.flagBluePosition = parsedMap.getPositionBlueFlag() != null ? parsedMap.getPositionBlueFlag().toVector3() : null;
        Vector3 vector3 = map.flagRedPosition = parsedMap.getPositionRedFlag() != null ? parsedMap.getPositionRedFlag().toVector3() : null;
        if (map.flagBluePosition != null) {
            map.flagBluePosition.z += 50.0f;
            map.flagRedPosition.z += 50.0f;
        }
        if (parsedMap.getPoints() != null) {
            map.domKeypoints = parsedMap.getDOMKeypoints();
        }
        maps.put(map.id, map);
    }

    private static IMapConfigItem getMapItem(String id) {
        for (IMapConfigItem item : configItems) {
            if (!item.id.equals(id)) continue;
            return item;
        }
        return null;
    }
}

