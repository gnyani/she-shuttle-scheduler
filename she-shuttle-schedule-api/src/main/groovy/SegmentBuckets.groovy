import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * Created by GnyaniMac on 08/03/18.
 */

@ToString
@EqualsAndHashCode
@TupleConstructor
class SegmentBuckets {

    SegmentBuckets(){
        this.segmentBuckets = new HashMap<Segment,List<Bucket>>()
    }

    HashMap<Segment,List<Bucket>> segmentBuckets
}
