package gtanks.kafka.extermalMessage;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GetChallengeInfoRequest {
    private final Long userId;
}
