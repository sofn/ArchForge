package ${packageBase}.dto;

import java.util.List;
import lombok.Data;

@Data
public class ${entityName}PageResult<T> {

    private List<T> list;

    private long total;

    private int pageSize;

    private int currentPage;

    public static <T> ${entityName}PageResult<T> of(List<T> list, long total, int pageSize, int currentPage) {
        ${entityName}PageResult<T> result = new ${entityName}PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        return result;
    }
}
