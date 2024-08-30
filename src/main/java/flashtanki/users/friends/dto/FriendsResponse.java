package flashtanki.users.friends.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendsResponse {
    @Builder.Default
    private List<FriendInfo> incoming = new ArrayList<>();
    @Builder.Default
    private List<FriendInfo> outgoing = new ArrayList<>();
    @Builder.Default
    private List<FriendInfo> friends = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FriendInfo {

        private String battleId;
        private int rank;
        private boolean isPremium;
        private boolean online;
        private String id;//nickname

    }
}
