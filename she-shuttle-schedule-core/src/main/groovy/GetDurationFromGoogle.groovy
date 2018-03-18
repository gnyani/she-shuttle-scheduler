import groovy.json.JsonSlurper

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Created by GnyaniMac on 14/03/18.
 */
class GetDurationFromGoogle {

    Duration getDuration(StopDetails origin, StopDetails destination, LocalTime departure_time, Date date){

        println("getting traffic data from google between ${origin.stopName} and ${destination.stopName}")

        try{
            def responseJson = new JsonSlurper().parseText(new URL("https://maps.googleapis.com/maps/api/distancematrix/json?${formURL(origin,destination,departure_time,date)}").getText())

            def seconds = responseJson.rows.elements.duration_in_traffic.value[0][0]

            Duration duration = new Duration((long)seconds,0)

            return duration
        }catch (Exception e){

            println(e)

            Duration duration1 = new Duration(0,0)

            return  duration1
        }

    }

    String formURL(StopDetails origin,StopDetails destination,LocalTime departure_time,Date date){


        Properties properties = new Properties()


        this.getClass().getResource( '/application.properties' ).withInputStream {
            properties.load(it)
        }

        String API_KEY = properties."google.distance.matrix.api.key".toString()

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        StringBuilder stringBuilder = new StringBuilder()

        Instant instant = departure_time.atDate(LocalDate.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))).
                atZone(ZoneId.systemDefault()).toInstant();
        Date time = Date.from(instant);

        long timeInSeconds = time.getTime()

        stringBuilder.append("origins=${origin.latitude.trim()},${origin.longitude.trim()}&").append("destinations=${destination.latitude.trim()},${destination.longitude.trim()}&").append("departure_time=${timeInSeconds}&")
        .append("mode=driving&").append("traffic_model=best_guess&").append("key=${API_KEY}")


        stringBuilder.toString()
    }
}
