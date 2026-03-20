package backend.modules.registration.dto;

import lombok.Data;

@Data
public class RegistrationQueryDTO {
    private Integer pageNum  = 1;
    private Integer pageSize = 10;
    /** null 表示不限 */
    private Integer status;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
