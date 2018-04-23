
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Export {
    private Font headerFont;
    private CellStyle headerCellstyle;
    private Map<String, String> harvestedLinkMap;
    private Map<String, Integer> wordCountLinkMap;
    private Map<String, Integer> titleWordCountLinkMap;
    private Workbook excel;
    private static String[] columnsWordFrequency = {"ID", "Word", "Frequency"};
    private static String[] columnsWebLinks = {"ID", "Title", "URL"};

    public Export(Map<String, String> harvestedLinks, Map<String, Integer> titleWordCount,  Map<String, Integer> wordCount) throws IOException {
        this.harvestedLinkMap = new HashMap<String, String>();
        this.harvestedLinkMap.putAll(harvestedLinks);

        this.wordCountLinkMap = new HashMap<String, Integer>();
        this.wordCountLinkMap.putAll(wordCount);

        this.titleWordCountLinkMap = new HashMap<String,Integer>();
        this.titleWordCountLinkMap.putAll(titleWordCount);

        this.excel = new XSSFWorkbook();

        this.headerFont = excel.createFont();
        this.headerFont.setBold(true);
        this.headerFont.setFontHeightInPoints((short) 15);

        this.headerCellstyle = excel.createCellStyle();
        this.headerCellstyle.setFont(headerFont);

        writeRAWwebLinks();
        writeRAWWordCount();

        FileOutputStream fileOut = new FileOutputStream("RelevancyConnectionPointList.xlsx");
        System.out.println("Writing: Data to Workbook");
        excel.write(fileOut);

        System.out.println("Closing FileOutStream");
        fileOut.close();

        System.out.println("Closing Workbook");
        excel.close();

    }


    //Creates the sheet for WordCount, uses populate method to create the rows and data
    private void writeRAWWordCount() throws IOException {
        Sheet rawWordFrequency = excel.createSheet("RAW_WordFrequency");
        Sheet rawTitleWordFrequency = excel.createSheet("RAW_TitleWordFrequency");

        Row headerRow = rawWordFrequency.createRow(0);
        Row titleHeaderRow = rawTitleWordFrequency.createRow(0);
        for (int i = 0; i < columnsWordFrequency.length; i++) {

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnsWordFrequency[i]);
            cell.setCellStyle(headerCellstyle);

            Cell secCell = titleHeaderRow.createCell(i);
            secCell.setCellValue(columnsWordFrequency[i]);
            secCell.setCellStyle(headerCellstyle);
        }

        //Populates through the hashmaps respectively
        populateWordCountRAW(rawWordFrequency, wordCountLinkMap);
        populateWordCountRAW(rawTitleWordFrequency,titleWordCountLinkMap);
    }

    //Creates the rows and data based on the relevant hashmap
    private void populateWordCountRAW(Sheet wordSheet, Map<String,Integer> wordMap) {
        int rowNum = 1;
        Iterator it = wordMap.entrySet().iterator();
        Map.Entry pair;
        while (it.hasNext()) {
            pair = (Map.Entry) it.next();

            Row row = wordSheet.createRow(rowNum++);

            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue((String) pair.getKey());
            row.createCell(2).setCellValue((Integer) pair.getValue());

            //outputs pair : value key
            //System.out.println(pair.getKey() + " : " + pair.getValue());
            if ((rowNum%25000) == 0 || (wordMap.size() - rowNum) < 10) {
                System.out.println("Words: " + (wordMap.size() - rowNum) + " left");
            }
        }

    }

    //Creates the sheet for Weblinks, uses populate method to create the rows and data
    private void writeRAWwebLinks() {
        Sheet rawWebLinks = excel.createSheet("RAW_WebLinks");

        Row headerRow = rawWebLinks.createRow(0);

        for (int i = 0; i < columnsWebLinks.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnsWebLinks[i]);
            cell.setCellStyle(headerCellstyle);
        }

        populateWebLinkRAW(rawWebLinks);
    }

    //Creates the rows and data based on the hashmap
    private void populateWebLinkRAW(Sheet rawWebLinks) {

        Iterator it = harvestedLinkMap.entrySet().iterator();
        Map.Entry pair;
        int rowNum = 1;
        while (it.hasNext()) {
            pair = (Map.Entry) it.next();

            Row row = rawWebLinks.createRow(rowNum++);

            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue((String) pair.getValue());
            row.createCell(2).setCellValue((String) pair.getKey());

            if (rowNum % 25000 == 0 || (harvestedLinkMap.size() - rowNum) < 100) {
                System.out.println("Links: " + (harvestedLinkMap.size() - rowNum) + " left");
            }
        }

        for(int i = 0; i < columnsWebLinks.length;i++) {
            rawWebLinks.autoSizeColumn(i);
        }
    }
}

