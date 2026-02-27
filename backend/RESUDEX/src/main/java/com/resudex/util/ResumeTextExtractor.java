package com.resudex.util;

import org.springframework.web.multipart.MultipartFile;

public class ResumeTextExtractor {

    public static String extractText(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename().toLowerCase();

            if (filename.endsWith(".pdf")) {
                return PdfUtil.extractText(file.getInputStream());
            }

            if (filename.endsWith(".docx")) {
                return DocxUtil.extractText(file.getInputStream());
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }
}