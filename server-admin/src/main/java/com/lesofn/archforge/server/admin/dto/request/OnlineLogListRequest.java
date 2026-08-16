package com.lesofn.archforge.server.admin.dto.request;

import com.lesofn.archforge.server.admin.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 在线用户查询请求
 *
 * @author lesofn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OnlineLogListRequest extends BasePageRequest {

    /** 用户名 */
    private String username;
}
