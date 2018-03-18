import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalTime

/**
 * Created by GnyaniMac on 06/03/18.
 */

@EqualsAndHashCode
@ToString
class StopInstance {

    String stopName

    String stopId

    LocalTime startTime
}
