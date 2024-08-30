package gtanks.containers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListContainerResponse {
    private List<ContainerInfo> list;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContainerInfo {
        private String id;
        private String title;
        private String desc;
        private int count;
    }

    

}
