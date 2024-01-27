package com.bdilab.storage.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class PathConfig     {
    @Value("${document.path.base.pdf}")
    public String pdfDocumentPathBase;

    @Value("${document.path.base.word}")
    public String wordDocumentPathBase;

    @Value("${document-qa-server.endpoint.add-report}")
    private String addReportEndpoint;
}
