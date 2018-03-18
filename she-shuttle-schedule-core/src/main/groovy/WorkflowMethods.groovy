import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import static java.time.temporal.ChronoUnit.SECONDS;

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Created by GnyaniMac on 06/03/18.
 */

class WorkflowMethods {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy")

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.getDefault());

    SegmentBuckets allSegmentBuckets = new SegmentBuckets()


    public HashMap<Segment,List<Bucket>> readExcelData(String path){

        Properties properties = new Properties()


        this.getClass().getResource( '/application.properties' ).withInputStream {
            properties.load(it)
        }


        Workbook workbook = WorkbookFactory.create(new File(path));
        def dataPointsSheet = workbook.getAt(0)

        DataFormatter dataFormatter = new DataFormatter()
        def datesRow = dataPointsSheet[1]
        2.upto(datesRow.size()){
            def col = it
            2.upto(dataPointsSheet.size()){
                String sourceNameFromCell = dataFormatter.formatCellValue(dataPointsSheet[it]?.getCell(0))
                String destNameFromCell = dataFormatter.formatCellValue(dataPointsSheet[it+1]?.getCell(0))
                String starttimeFromCell = dataFormatter.formatCellValue(dataPointsSheet[it]?.getCell(col))
                String endtimeFromCell = dataFormatter.formatCellValue(dataPointsSheet[it+1]?.getCell(col))
                String dateFromCell = datesRow[col]
                if(sourceNameFromCell == destNameFromCell){
                    destNameFromCell = dataFormatter.formatCellValue(dataPointsSheet[it+2]?.getCell(0))
                    endtimeFromCell = dataFormatter.formatCellValue(dataPointsSheet[it+2]?.getCell(col))
                }
                if(dateFromCell && sourceNameFromCell && destNameFromCell && sourceNameFromCell != destNameFromCell)
                {
                    Segment segment = new Segment(sourceNameFromCell,destNameFromCell)
                    if(checkBucketsExist(segment))
                    {
                        if(starttimeFromCell && endtimeFromCell){

                            String startTimein24hr = LocalTime.parse(starttimeFromCell,timeFormatter).format(DateTimeFormatter.ofPattern("HH:mm"));
                            String endTimein24hr = LocalTime.parse(endtimeFromCell,timeFormatter).format(DateTimeFormatter.ofPattern("HH:mm"));


                            LocalTime endTime = LocalTime.parse(endTimein24hr)
                            LocalTime startTime = LocalTime.parse(startTimein24hr)

                            Double totalTripTime = null

                            if(endTimein24hr >= startTimein24hr)
                            {
                                totalTripTime =  startTime.until(endTime, SECONDS)/60
                                TripDurations tripDurations = new TripDurations(totalTripTime,dateFormatter.parse(dateFromCell),LocalTime.parse(startTimein24hr),LocalTime.parse(endTimein24hr))
                                bucketizeTripDurations(tripDurations,segment)
                            }
                            else{
                                println("endtime cannot be less than start time ${endtimeFromCell} and ${starttimeFromCell}")
                            }

                        }
                    }else{
                        int numberOfBuckets = generateBucketsCount(properties)
                        createBucketObjects(numberOfBuckets, properties, segment)
                    }
                }

            }
        }
//        println("count of segments ${allSegmentBuckets.segmentBuckets.size()} \n")
//        prettyPrintAnalysis()
        allSegmentBuckets.segmentBuckets
    }

    private void prettyPrintAnalysis() {
        allSegmentBuckets.segmentBuckets.each {
            println("${it.key} \n")
            def bucketList = it.value
            bucketList.each {
                println("Bucket Start Time ${it.bucketStartTime} \n")
                def tripsInBucket = it.tripDurationsList
                if (tripsInBucket.size() > 0) {
                    println("Total Trips in this bucket ${tripsInBucket.size()} ")
                    tripsInBucket.each {
                        println("Trip Start Time ${it.sourceTime}")
                        println("Trip End Time ${it.destTime}")
                        println("Trip Duration ${it.duration} in Minutes")
                        println("Trip Date ${it.date} \n")
                    }
                }else{
                    println("No Trips  in this Bucket \n")
                }
                println("Bucket End Time ${it.bucketEndTime} \n \n")
            }
        }
    }


    boolean  checkBucketsExist(Segment segment){

        allSegmentBuckets.segmentBuckets.containsKey(segment)
    }

    void createBucketObjects(int number, Properties properties, Segment segment){

        int startTime = Integer.parseInt(properties."she.shuttle.scheduler.startTime".toString())
        int interval = Integer.parseInt(properties."she.shuttle.scheduler.timeInterval".toString())

        int bucketStartTime = startTime

        LocalTime bucketStartInLocalTime = LocalTime.of(bucketStartTime,0,0)

        def buckets = []

        while(number--){
            Bucket b = new Bucket()
            b.bucketStartTime = bucketStartInLocalTime
            Duration d = new Duration(interval*60,0)
            b.bucketEndTime = b.bucketStartTime + d
            bucketStartInLocalTime = bucketStartInLocalTime + d
            buckets.add(b)
        }
        allSegmentBuckets.segmentBuckets.put(segment,buckets)
    }

    void bucketizeTripDurations(TripDurations tripDuration,Segment segment){

        def bucketList = allSegmentBuckets.segmentBuckets.get(segment)

        for(int i=0;i<bucketList.size()-1;i++) {
            if (bucketList[i].bucketStartTime <= tripDuration.sourceTime && tripDuration.sourceTime <= bucketList[i].bucketEndTime)
                if((tripDuration.sourceTime.until(bucketList[i].bucketEndTime,SECONDS)) >= (bucketList[i].bucketEndTime.until(tripDuration.destTime, SECONDS)) )
                {
                    bucketList[i].tripDurationsList.add(tripDuration)
                    break
                }
                else{
                  bucketList[i+1].tripDurationsList.add(tripDuration)
                    break
                }
            }
        }


    int generateBucketsCount(Properties properties){

        int buckets = 0

        int startTime = Integer.parseInt(properties."she.shuttle.scheduler.startTime".toString())
        int interval = Integer.parseInt(properties."she.shuttle.scheduler.timeInterval".toString())
        int endTime = Integer.parseInt(properties."she.shuttle.scheduler.endTime".toString())
        if(interval == 0 )
            println("Interval cannot be 0 minutes")
        else if(endTime < startTime)
            println("endTime should be greater than start time")
        else{
            buckets = (endTime - startTime)*60/interval
        }
        buckets
    }
}
