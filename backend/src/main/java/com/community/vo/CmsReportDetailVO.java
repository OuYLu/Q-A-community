package com.community.vo;

import com.community.entity.CmsReport;
import lombok.Data;

@Data
public class CmsReportDetailVO {
    private CmsReport report;
    private CmsReportContentVO content;
    private CmsReportAuthorVO author;
}
