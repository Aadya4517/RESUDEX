package com.resudex.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;

public class DocxUtil {

    public static String extractText(InputStream inputStream) {
        try {
            XWPFDocument document = new XWPFDocument(inputStream);
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(p -> text.append(p.getText()).append("\n"));
            document.close();
            return text.toString();
        } catch (Exception e) {
            return "";
        }
    }
}