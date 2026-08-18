package com.lesofn.archforge.server.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebUserProfileResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
}
