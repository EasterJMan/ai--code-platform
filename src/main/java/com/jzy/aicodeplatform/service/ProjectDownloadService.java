package com.jzy.aicodeplatform.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {
    void downloadProjectAsZip(String sourceDirPath, String downloadFileName, HttpServletResponse response);
}
