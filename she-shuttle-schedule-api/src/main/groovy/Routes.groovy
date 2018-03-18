import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * Created by GnyaniMac on 10/03/18.
 */
@ToString
@EqualsAndHashCode
@TupleConstructor
class Routes {

    Routes(){
        this.allRoutes = new LinkedHashMap<String,Route>()
    }

    LinkedHashMap<String,Route> allRoutes
}
