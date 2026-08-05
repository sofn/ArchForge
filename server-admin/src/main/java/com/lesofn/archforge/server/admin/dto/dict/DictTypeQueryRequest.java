package com.lesofn.archforge.server.admin.dto.dict;

import com.lesofn.archforge.server.admin.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQueryRequest extends PageQuery {

    @Nullable
    private String keyword;
}
