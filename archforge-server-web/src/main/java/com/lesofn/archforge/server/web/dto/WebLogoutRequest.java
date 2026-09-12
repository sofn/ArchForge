package com.lesofn.archforge.server.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class WebLogoutRequest {

    private String refreshToken;
}
