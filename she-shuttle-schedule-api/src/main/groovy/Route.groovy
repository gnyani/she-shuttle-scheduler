import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * Created by GnyaniMac on 10/03/18.
 */
@ToString
@EqualsAndHashCode
@TupleConstructor
class Route {

    Route(){
        this.stopsOrder = new ArrayList<StopDetails>()
    }

    String routeName

    List<StopDetails> stopsOrder
}
