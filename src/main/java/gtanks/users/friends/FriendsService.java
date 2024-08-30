package gtanks.users.friends;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.main.params.OnlineStats;
import gtanks.premium.PremiumService;
import gtanks.services.LobbysServices;
import gtanks.users.User;
import gtanks.users.UserRepository;
import gtanks.users.friends.dto.FriendsResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.common.protocol.types.Field;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FriendsService {
    private static FriendsService instance;
    private final PremiumService premiumService = PremiumService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final FriendsRepository friendsRepository = FriendsRepository.getInstance();
    private final LobbysServices lobbysServices = LobbysServices.getInstance();

    public static FriendsService getInstance() {
        if (instance == null) {
            instance = new FriendsService();
        }
        return instance;
    }

    private FriendsService() {
    }

    @SneakyThrows
    public Friends getDefaultFriends(long userId) {
        String empty = "[]";
        return Friends.builder()
                .userId(userId)
                .accepted(empty)
                .incoming(empty)
                .outgoing(empty)
                .build();
    }

    @SneakyThrows
    public FriendsResponse getFriendsByUser(long userId) {
        Friends friendsByUser = friendsRepository.getFriendsByUser(userId)
                .orElseGet(() -> getDefaultFriends(userId));
        ObjectMapper objectMapper = new ObjectMapper();
        Long[] incomingFriends = objectMapper.readValue(friendsByUser.getIncoming(), Long[].class);
        Long[] outgoingFriends = objectMapper.readValue(friendsByUser.getOutgoing(), Long[].class);
        Long[] acceptedFriends = objectMapper.readValue(friendsByUser.getAccepted(), Long[].class);

        List<Long> usersIds = Stream.of(incomingFriends, outgoingFriends, acceptedFriends)
                .flatMap(Arrays::stream)
                .toList();
        Map<Long, UserRepository.UserRank> usersNicknamesAndRanks = userRepository.getUsersNicknamesAndRanks(usersIds);

        FriendsResponse response = FriendsResponse.builder()
                .incoming(Arrays.stream(incomingFriends)
                        .map(uId ->
                                FriendsResponse.FriendInfo.builder()
                                        .id(usersNicknamesAndRanks.get(uId).getNickname())
                                        .rank(usersNicknamesAndRanks.get(uId).getRank() + 1)
                                        .isPremium(premiumService.getPremiumTime(uId).isActivated())
                                        .build()
                        )
                        .toList())
                .outgoing(Arrays.stream(outgoingFriends)
                        .map(uId ->
                                FriendsResponse.FriendInfo.builder()
                                        .id(usersNicknamesAndRanks.get(uId).getNickname())
                                        .rank(usersNicknamesAndRanks.get(uId).getRank() + 1)
                                        .isPremium(premiumService.getPremiumTime(uId).isActivated())

                                        .build()
                        )
                        .toList())
                .friends(Arrays.stream(acceptedFriends)
                        .map(uId ->
                                FriendsResponse.FriendInfo.builder()
                                        .id(usersNicknamesAndRanks.get(uId).getNickname())
                                        .rank(usersNicknamesAndRanks.get(uId).getRank() + 1)
                                        .isPremium(premiumService.getPremiumTime(uId).isActivated())
                                        .build()
                        )
                        .toList())
                .build();


        response.getFriends().forEach(friendInfo -> {
            LobbyManager lobby = lobbysServices.getLobbyByNick(friendInfo.getId());

            friendInfo.setOnline(OnlineStats.inOnline(friendInfo.getId()));
            String battleId = lobby != null ? (lobby.battle != null ? lobby.battle.battle != null ? lobby.battle.battle.battleInfo != null ? lobby.battle.battle.battleInfo.battleId : null : null : null) : null;
            friendInfo.setBattleId(battleId);
        });
        return response;

    }

    @SneakyThrows
    public void addFriend(long userId, String friendNick) {
        Friends friendsFirst = friendsRepository.getFriendsByUser(userId)
                .orElseGet(() -> getDefaultFriends(userId));

        User newFriend = userRepository.findUserByNickname(friendNick);
        if (newFriend == null) {
            return;
        }
        Friends friendsSecond = friendsRepository.getFriendsByUser(newFriend.getId())
                .orElseGet(() -> getDefaultFriends(newFriend.getId()));

        ObjectMapper objectMapper = new ObjectMapper();

        friendsFirst.setOutgoing(addToFriends(friendsFirst.getOutgoing(), newFriend.getId()));
        friendsSecond.setIncoming(addToFriends(friendsSecond.getIncoming(), userId));
        friendsRepository.save(friendsFirst);
        friendsRepository.save(friendsSecond);

        LobbysServices.getInstance().getLobbyByUserId(userId).send(
                Type.LOBBY,
                "update_friends_list",
                objectMapper.writeValueAsString(getFriendsByUser(userId))
        );

        if (OnlineStats.inOnline(friendNick)) {
            LobbysServices.getInstance().getLobbyByUser(newFriend).send(Type.LOBBY, "show_friends_warning", friendsSecond.getIncoming());
        }

    }

    @SneakyThrows
    public void danyFriend(long userId, String danyFriendNickname) {
        Friends friends = friendsRepository.getFriendsByUser(userId).orElseGet(() -> getDefaultFriends(userId));
        User danyUser = userRepository.findUserByNickname(danyFriendNickname);
        if (danyUser == null) {
            return;
        }
        Friends friendsByDenyUser = friendsRepository
                .getFriendsByUser(danyUser.getId()).orElseGet(() -> getDefaultFriends(danyUser.getId()));

        friends.setIncoming(removeFromFriends(friends.getIncoming(), danyUser.getId()));
        friendsByDenyUser.setOutgoing(removeFromFriends(friendsByDenyUser.getOutgoing(), userId));

        friendsRepository.save(friends);
        friendsRepository.save(friendsByDenyUser);

        ObjectMapper objectMapper = new ObjectMapper();
        LobbysServices.getInstance().getLobbyByUserId(userId).send(
                Type.LOBBY,
                "update_friends_list",
                objectMapper.writeValueAsString(getFriendsByUser(userId))
        );
    }

    @SneakyThrows
    private String addToFriends(String friends, Long userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Long[] friendsArray = objectMapper.readValue(friends, Long[].class);
        Set<Long> friendsSet = new HashSet<>(Arrays.asList(friendsArray));
        friendsSet.add(userId);
        return objectMapper.writeValueAsString(friendsSet);
    }

    @SneakyThrows
    private String removeFromFriends(String friends, Long userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Long[] friendsArray = objectMapper.readValue(friends, Long[].class);
        Set<Long> friendsSet = new HashSet<>(Arrays.asList(friendsArray));
        friendsSet.removeIf(friend -> friend.equals(userId));
        return objectMapper.writeValueAsString(friendsSet);
    }

    @SneakyThrows
    public void acceptFriend(long userId, String friendNickname) {
        Friends friends = friendsRepository.getFriendsByUser(userId).orElseGet(() -> getDefaultFriends(userId));
        User acceptedUser = userRepository.findUserByNickname(friendNickname);
        if (acceptedUser == null) {
            return;
        }
        Friends acceptedUserFriends = friendsRepository.getFriendsByUser(acceptedUser.getId()).orElseGet(() -> getDefaultFriends(acceptedUser.getId()));

        friends.setIncoming(removeFromFriends(friends.getIncoming(), acceptedUser.getId()));
        friends.setOutgoing(removeFromFriends(friends.getOutgoing(), acceptedUser.getId()));
        friends.setAccepted(addToFriends(friends.getAccepted(), acceptedUser.getId()));

        acceptedUserFriends.setOutgoing(removeFromFriends(acceptedUserFriends.getOutgoing(), userId));
        acceptedUserFriends.setIncoming(removeFromFriends(acceptedUserFriends.getIncoming(), userId));
        acceptedUserFriends.setAccepted(addToFriends(acceptedUserFriends.getAccepted(), userId));

        friendsRepository.save(friends);
        friendsRepository.save(acceptedUserFriends);

        ObjectMapper objectMapper = new ObjectMapper();
        LobbysServices.getInstance().getLobbyByUserId(userId).send(
                Type.LOBBY,
                "update_friends_list",
                objectMapper.writeValueAsString(getFriendsByUser(userId))
        );
    }

    @SneakyThrows
    public void delFriend(long userId, String deletedNickname) {
        Friends friends = friendsRepository.getFriendsByUser(userId).orElseGet(() -> getDefaultFriends(userId));
        User deletedUser = userRepository.findUserByNickname(deletedNickname);
        if (deletedUser == null) {
            return;
        }
        Friends deletedUserFriends = friendsRepository.getFriendsByUser(deletedUser.getId()).orElseGet(() -> getDefaultFriends(deletedUser.getId()));

        friends.setAccepted(removeFromFriends(friends.getAccepted(), deletedUser.getId()));
        deletedUserFriends.setAccepted(removeFromFriends(deletedUserFriends.getAccepted(), userId));

        friendsRepository.save(friends);
        friendsRepository.save(deletedUserFriends);

        ObjectMapper objectMapper = new ObjectMapper();
        LobbysServices.getInstance().getLobbyByUserId(userId).send(
                Type.LOBBY,
                "update_friends_list",
                objectMapper.writeValueAsString(getFriendsByUser(userId))
        );
    }
}
