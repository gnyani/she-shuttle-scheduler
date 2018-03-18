/**
 * Created by GnyaniMac on 10/03/18.
 */
class FilterOutliers {


    void filter(HashMap<Segment,List<Bucket>> listHashMap){
        listHashMap.each {
            it.value.each {
                if(it.tripDurationsList.size()>0){
                    def durationsForSegement=[]
                    it.tripDurationsList.each {
                        durationsForSegement.add(it.duration)
                    }
                    Statistics statistics = new Statistics(durationsForSegement as double[])
                    Double stdDev = statistics.stdDev
                    //println("count before filter ${it.tripDurationsList.size()}")
                    def filteredList = it.tripDurationsList.findAll(){
                        (it.duration < 5*stdDev && it.duration < 30.00)
                    }
                    //println("count after filter ${filteredList.size()}")
                    it.tripDurationsList = filteredList
                }
            }
        }
    }
}
