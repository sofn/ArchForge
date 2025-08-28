package com.lesofn.matrixboot.deploy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token DTO
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 当前登录用户信息
     */
    private CurrentLoginUserDTO userInfo;
}