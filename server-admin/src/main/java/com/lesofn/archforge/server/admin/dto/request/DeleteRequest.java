package com.lesofn.archforge.server.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用删除请求
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRequest {

    private Long id;
}
