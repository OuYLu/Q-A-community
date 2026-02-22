package com.community.vo;

import com.community.entity.CmsAudit;
import lombok.Data;

@Data
public class CmsAuditDetailVO {
    private CmsAudit audit;
    private CmsAuditContentVO content;
    private CmsAuditAuthorVO author;
}
