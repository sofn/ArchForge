package com.lesofn.archforge.server.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOperationLogResponse {

    private Long id;
    private String username;
    private String module;
    private String summary;
    private LocalDateTime operatingTime;
}
