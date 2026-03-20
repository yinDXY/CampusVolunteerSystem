package backend.modules.activity.dto;

import lombok.Data;

@Data
public class ActivityQueryDTO {
    private Integer pageNum  = 1;
    private Integer pageSize = 10;
    private String  keyword;
    /** 按状态筛选，null 表示不限 */
    private Integer status;

    /** MyBatis 通过 getOffset() 取值，无需手动传入 */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
