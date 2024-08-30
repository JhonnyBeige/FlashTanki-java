package flashtanki.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GetStarsRequest {
    private final Long userId;
}
