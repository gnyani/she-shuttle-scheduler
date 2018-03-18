import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Created by GnyaniMac on 10/03/18.
 */
class ScheduleFlow {

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault());

    FinalSchedule finalSchedule = new FinalSchedule()

    ExcelWriter excelWriter = new ExcelWriter()

    GetDurationFromGoogle durationFromGoogle = new GetDurationFromGoogle()

    void startScheduleWorkflow(Routes allRoutes,HashMap<Segment,List<Bucket>> segmentListHashMap){

        int routeNumber = displayAvailableRoutes(allRoutes)

        LocalTime time = askForStartTime()

        computeSchedule(routeNumber-1,time,allRoutes,segmentListHashMap)

    }

    int displayAvailableRoutes(Routes routes){
        println("please select one of the routes")
        routes.allRoutes.eachWithIndex{ route , index ->
            println("${index+1}.${route.key}")
        }
        println("enter one of the route numbers you want to know the schedule estimate")
       try{
           Scanner input = new Scanner(System.in)
           int option = input.nextInt()
           if(option < 0 || option > routes.allRoutes.size()){
               println("Please enter a valid option from the above listed routes")
               displayAvailableRoutes(routes)
           }else{
               option
           }
       }catch (Exception e){
           println("Invalid input please select one of the number from the options given \n")
           displayAvailableRoutes(routes)
       }

    }

    LocalTime askForStartTime(){
        println("Enter start time of the trip in a proper 24h format ex: 7:30")

        Scanner input = new Scanner(System.in)
        String time = input.next()
        try{
            LocalTime startTime = LocalTime.parse(time,timeFormatter);
            return startTime
        }catch (DateTimeParseException e){
            println("Time entered could not be parsed ${e} try again")
            askForStartTime()
        }
    }

    LocalTime askForStartTimeAndValidate(LocalTime stime){

        println("Enter start time of the trip greater than ${stime}")

        Scanner input = new Scanner(System.in)
        String time = input.next()
        LocalTime startTime
        try{
             startTime = LocalTime.parse(time,timeFormatter);
        }catch (DateTimeParseException e){
            println("Time entered could not be parsed ${e} try again")
            askForStartTimeAndValidate(stime)
        }
        if(startTime < stime){
            println("start time cannot be less than ${stime} please try again")
            askForStartTimeAndValidate(stime)
        }else{
            return startTime
        }

    }

    void computeSchedule(int routeNumber, LocalTime startTime, Routes routes, HashMap<Segment,List<Bucket>> segmentListHashMap, int counter = 0){

        LocalTime currentTime = startTime

        ArrayList<Route> allRoutes = routes.allRoutes.values()

        Route currentRoute =  allRoutes[routeNumber]

        println("Computing estimated schedule for the selected route ${currentRoute.routeName} starting at time ${startTime}")

        println("Printing Schedule... \n \n")

        boolean  flag = false
        for(int index = counter ; index< currentRoute.stopsOrder.size() ; index++){

            println("${index+1}.${currentRoute.stopsOrder[index].stopName}   at  ${currentTime}")

            StopInstance stopInstance = new StopInstance()
            stopInstance.stopName = currentRoute.stopsOrder[index].stopName
            stopInstance.stopId = currentRoute.stopsOrder[index].stopId
            stopInstance.startTime = currentTime

            finalSchedule.allStopsSchedule.put(index, stopInstance)

            if(!flag)
                counter = index
            flag = true

            if(currentRoute.stopsOrder[index+1]){
                if(currentRoute.stopsOrder[index].stopName.trim() != currentRoute.stopsOrder[index+1].stopName.trim()){
                    Segment segment = new Segment()
                    segment.source = currentRoute.stopsOrder[index].stopName
                    segment.destination  = currentRoute.stopsOrder[index+1].stopName
                    List<Bucket> segmentBuckets = segmentListHashMap.get(segment)
                    if(segmentBuckets != null){
                        Double averageDuration = findBucketBasedOnTimeAndCalculateAverage(startTime,segmentBuckets)
                        if(averageDuration > 0)
                            currentTime = currentTime + new Duration((long)averageDuration*60,0)
                        else if(averageDuration == (double)0){
                            Duration duration1 = durationFromGoogle.getDuration(currentRoute.stopsOrder[index],currentRoute.stopsOrder[index+1],currentTime,new Date())
                            println(duration1.toMinutes())
                            if(duration1.toMinutes() != 0 ){
                                boolean  takeManual = okWithGoogleResult()
                                if(!takeManual){
                                    double duration = checkManualInput()
                                    currentTime = currentTime + new Duration((long)duration*60,0)
                                }else{
                                    currentTime = currentTime + duration1
                                }
                            }else{
                                double duration = checkManualInput()
                                currentTime = currentTime + new Duration((long)duration*60,0)
                            }
                        }
                    }else{
                        println("segment not found")
                        Duration duration1 = durationFromGoogle.getDuration(currentRoute.stopsOrder[index],currentRoute.stopsOrder[index+1],currentTime,new Date())
                        println(duration1.toMinutes())
                        if(duration1.toMinutes() != 0 ){
                            boolean  takeManual = okWithGoogleResult()
                            if(!takeManual){
                                double duration = checkManualInput()
                                currentTime = currentTime + new Duration((long)duration*60,0)
                            }else{
                                currentTime = currentTime + duration1
                            }
                        }else{
                            double duration = checkManualInput()
                            currentTime = currentTime + new Duration((long)duration*60,0)
                        }
                    }

                }else{
                    currentTime = checkScheduleReturn(routeNumber, startTime, currentTime, routes, segmentListHashMap,counter)
                    counter = index+1
                    startTime = currentTime
                }
            }

        }
        excelWriter.createScheduleExcel(finalSchedule.allStopsSchedule)
    }

    boolean okWithGoogleResult(){
        println("Press Y if you are okay with the google result or Press N to give manual input")
        Scanner input = new Scanner(System.in)
        def userInput = input.next()
        if(userInput == 'Y')
            return true
        else if(userInput == 'N')
            return false
        else{
            println("Please enter a valid input")
            okWithGoogleResult()
        }
    }

    private double checkManualInput() {
        println("Either average duration is 0  or this segment does not exist please give estimate for this in minutes")
        try {

            Scanner input = new Scanner(System.in)
            Double duration = input.nextDouble()
            return  duration
        }catch (Exception e){
            println("Please enter valid duration in minutes")
            checkManualInput()
        }
    }

    private double takeDurationBeforeStart() {
        println("Enter time in minutes before starting next trip")
        try {

            Scanner input = new Scanner(System.in)
            Double duration = input.nextDouble()
            return  duration
        }catch (Exception e){
            println("Please enter valid duration in minutes")
            takeDurationBeforeStart()
        }
    }

    private LocalTime checkScheduleReturn(int routeNumber, LocalTime startTime,LocalTime currentTime, Routes routes, HashMap<Segment, List<Bucket>> segmentListHashMap,int counter){

        println("Your Trip has completed successfully at ${currentTime}  would you like to plan the return? \n")

        println("Press Y to continue N to try out new schedule SF to start from beginning")

        Scanner input = new Scanner(System.in)

        String option = input.next()

        if(option == 'Y'){
            double duration = takeDurationBeforeStart()
            currentTime = currentTime + new Duration((long)duration*60,0)
            return currentTime
        }else if(option == 'N'){
            LocalTime time = askForStartTimeAndValidate(startTime)
            computeSchedule(routeNumber, time, routes, segmentListHashMap,counter)
        }else if(option == 'SF'){
            LocalTime time = askForStartTime()
            computeSchedule(routeNumber, time, routes, segmentListHashMap, 0)
        }
        else{
            println("Invalid Input Please Try again .. \n")
            checkScheduleReturn(routeNumber,startTime,currentTime,routes,segmentListHashMap,counter)
        }
    }

    static int findBucketBasedOnTimeAndCalculateAverage(LocalTime startTime, List<Bucket> segmentBuckets){
        int index

        for(int i=0;i<segmentBuckets.size();i++){
                if(segmentBuckets[i].bucketStartTime <= startTime && startTime <= segmentBuckets[i].bucketEndTime){
                    index = i
                    break
                }
            }

        if(index){
            def tripDurations =[]
            segmentBuckets[index].each {
                it.tripDurationsList.each {
                    tripDurations.add(it.duration)
                }
            }
            Statistics statistics = new Statistics(tripDurations as double[])
            return statistics.mean
        }
        else{
            println("This case should not have come something is missed by me")
        }
    }
}
