package flashtanki.users.groups;

import flashtanki.logger.remote.types.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.users.TypeUser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class UserGroupsLoader {
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final String FILE_FORMAT = ".group";
    private static HashMap<TypeUser, UserGroup> usersGroups = new HashMap();

    public static UserGroup getUserGroup(TypeUser typeUser) {
        return usersGroups.get((Object)typeUser);
    }

    public static void load(String path) {
        File folder = new File(path);
        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(FILE_FORMAT)) continue;
            UserGroupsLoader.parseFile(file);
        }
    }

    private static void parseFile(File file) {
        try {
            List<String> avaliableChatCommands = Files.readAllLines(file.toPath());
            TypeUser typeUser = UserGroupsLoader.getTypeUser(file.getName().split(FILE_FORMAT)[0]);
            if(typeUser == null){
                return;
            }
            UserGroup userGroup = new UserGroup(avaliableChatCommands);
            userGroup.setGroupName(typeUser.toString());
            usersGroups.put(typeUser, userGroup);
            loggerService.log(LogType.INFO,"User group " + typeUser.toString() + " has been inited.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TypeUser getTypeUser(String name) {
        for (TypeUser type : TypeUser.values()) {
            if (!type.toString().equals(name)) continue;
            return type;
        }
        return null;
    }
}

