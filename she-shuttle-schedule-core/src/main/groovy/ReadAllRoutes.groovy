import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Created by GnyaniMac on 10/03/18.
 */

@ToString
@EqualsAndHashCode
@TupleConstructor
class ReadAllRoutes {

     Routes readAllRoutesFromExcel(){
         Properties properties = new Properties()


         this.getClass().getResource( '/application.properties' ).withInputStream {
             properties.load(it)
         }
         Workbook workbook = WorkbookFactory.create(new File(properties."she.shuttle.scheduler.input.routes".toString()));
         Sheet routesSheet = workbook.getAt(0)
         println(routesSheet.getSheetName())
         DataFormatter dataFormatter = new DataFormatter()
         Row routeNamesRow =  routesSheet[0]
         Routes routes = new Routes()
         0.step(routeNamesRow.size()-1,4){
             def col = it
             Route route = new Route()
             route.routeName = dataFormatter.formatCellValue(routeNamesRow.getCell(col))
             2.upto(routesSheet.size()-1){
                 StopDetails stopDetails = new StopDetails()
                 stopDetails.stopName = dataFormatter.formatCellValue(routesSheet[it].getCell(col))
                 stopDetails.stopId = dataFormatter.formatCellValue(routesSheet[it].getCell(col+1))
                 stopDetails.latitude = dataFormatter.formatCellValue(routesSheet[it].getCell(col+2))
                 stopDetails.longitude = dataFormatter.formatCellValue(routesSheet[it].getCell(col+3))
                 route.stopsOrder.add(stopDetails)
             }
             routes.allRoutes.put(route.routeName,route)
         }
         routes
     }
}
