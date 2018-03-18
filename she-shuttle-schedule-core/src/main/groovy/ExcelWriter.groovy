import constants.ExcelTypes
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook


/**
 * Created by GnyaniMac on 09/03/18.
 */
class ExcelWriter {

    public createOutputExcel(HashMap<Segment,List<Bucket>> listHashMap){

        Properties properties = new Properties()


        this.getClass().getResource( '/application.properties' ).withInputStream {
            properties.load(it)
        }

        Workbook workbook = new XSSFWorkbook();

        generateExcelSheets(workbook,listHashMap)
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(properties."she.shuttle.scheduler.output.basepath".toString()+"analysis.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    void generateExcelSheets(Workbook workbook,HashMap<Segment,List<Bucket>> listHashMap){

        println("writing the analysis output to excel .... ")

        createExcelSheet(workbook,listHashMap,ExcelTypes.DATA_POINTS)
        createExcelSheet(workbook,listHashMap,ExcelTypes.AVERAGES)
        createExcelSheet(workbook,listHashMap,ExcelTypes.STANDARD_DEVIATION)
    }

    void createExcelSheet(Workbook workbook, HashMap<Segment, List<Bucket>> listHashMap, ExcelTypes excelTypes){

        Statistics statistics = null

        // Create a Sheet
        Sheet sheet = null
        switch (excelTypes){
            case excelTypes.DATA_POINTS : sheet = workbook.createSheet(ExcelTypes.DATA_POINTS.toString());
                                          break
            case excelTypes.AVERAGES  :   sheet = workbook.createSheet(ExcelTypes.AVERAGES.toString());
                                          break
            case excelTypes.STANDARD_DEVIATION:  sheet = workbook.createSheet(ExcelTypes.STANDARD_DEVIATION.toString());
                                                 break
        }



        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont)

        // Create a Row
        Row headerRow = sheet.createRow(0)

        Map.Entry<Segment,List<Bucket>> entry = listHashMap.entrySet().iterator().next()

        int numberOfBuckets = entry.value.size()

        // Creating cells
        for(int i = 0; i < numberOfBuckets+1; i++) {
            Cell cell = headerRow.createCell(i);
            if(i==0){
                cell.setCellValue("Segments");
            }else{
                if(entry.value[i-1])
                cell.setCellValue("${entry.value[i-1].bucketStartTime}-${entry.value[i-1].bucketEndTime}")
            }
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1
        listHashMap.each {
            Row row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue("${it.key.source}-${it.key.destination}")
            int colNum = 1
            it.value.each{
                def listDurations = []
                it.tripDurationsList.each {
                    listDurations.add(it.duration)
                }
                switch (excelTypes){
                    case excelTypes.DATA_POINTS :row.createCell(colNum++).setCellValue(it.tripDurationsList.size())
                        break
                    case excelTypes.AVERAGES :
                        statistics = new Statistics(listDurations as double[])
                        row.createCell(colNum++).setCellValue(statistics.mean)
                        break
                    case excelTypes.STANDARD_DEVIATION:
                        statistics = new Statistics(listDurations as double[])
                        row.createCell(colNum++).setCellValue(statistics.stdDev)
                        break
                }

            }
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < numberOfBuckets+1; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    public createScheduleExcel(LinkedHashMap<Integer,StopInstance> scheduleMap){
        Properties properties = new Properties()


        this.getClass().getResource( '/application.properties' ).withInputStream {
            properties.load(it)
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Final_Schedule")


        // Create a Row
        Row headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Index")
        headerRow.createCell(1).setCellValue("StopId")
        headerRow.createCell(2).setCellValue("StopName")
        headerRow.createCell(3).setCellValue("Time")

        0.upto(scheduleMap.size()-1) {
            Row row = sheet.createRow(it+1)
            row.createCell(0).setCellValue(it+1)
            row.createCell(1).setCellValue(scheduleMap.get(it).stopId)
            row.createCell(2).setCellValue(scheduleMap.get(it).stopName)
            row.createCell(3).setCellValue(scheduleMap.get(it).startTime.toString())
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fileOut = new FileOutputStream(properties."she.shuttle.scheduler.output.basepath".toString()+"schedule.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

}
