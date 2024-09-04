package flashtanki.main.kafka.extermalMessage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GetChallengeInfoRequest {
    private final Long userId;
}
