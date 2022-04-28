/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-01-06 09:34:21
 */
@Slf4j
public class FileReader {

    public static String getPdfContent(File file) {
        String content = "";
        try (PDDocument document = new PDFParser(new RandomAccessReadBufferedFile(file)).parse()) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            content = stripper.getText(document);
        } catch (Throwable e) {
            throw  new RuntimeException(e);
        }
        return content;
    }

    public static String getWordContent(File file) {
        String buffer = "";
        try {
            if (file.getName().endsWith(".doc")) {
                InputStream is = new FileInputStream(file);
                WordExtractor ex = new WordExtractor(is);
                buffer = ex.getText();
                ex.close();
            } else if (file.getName().endsWith("docx")) {
                OPCPackage opcPackage = POIXMLDocument.openPackage(file.getAbsolutePath());
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                buffer = extractor.getText();
                extractor.close();
            } else {
                System.out.println("此文件不是word文件！");
            }
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }

        return buffer;
    }

    @Test
    public void test(){
        String wordContent = getWordContent(new File("C:/Users/Kylin/Documents/WXWork/1688852050011730/Cache/File/2021-06/标识系统.docx"));
        log.info(wordContent);
    }
}
