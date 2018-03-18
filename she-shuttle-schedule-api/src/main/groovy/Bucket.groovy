import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

import java.time.LocalTime

/**
 * Created by GnyaniMac on 08/03/18.
 */

@ToString
@EqualsAndHashCode
class Bucket {

    Bucket(){
        this.tripDurationsList = new ArrayList<TripDurations>()
    }

    LocalTime bucketStartTime

    LocalTime bucketEndTime

    List<TripDurations> tripDurationsList
}
