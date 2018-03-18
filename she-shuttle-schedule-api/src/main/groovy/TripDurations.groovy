import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

import java.time.LocalTime

/**
 * Created by GnyaniMac on 08/03/18.
 */

@ToString
@EqualsAndHashCode
@TupleConstructor
class TripDurations {
    Double duration
    Date date
    LocalTime sourceTime
    LocalTime destTime
}
