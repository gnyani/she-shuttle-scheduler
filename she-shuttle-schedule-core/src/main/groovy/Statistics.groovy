/**
 * Created by GnyaniMac on 10/03/18.
 */
public class Statistics {
    double[] data;
    int size;

    public Statistics(double[] data) {
        this.data = data;
        size = data.length;
    }

    double getMean() {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        if(sum>0)
        return sum/size
        return sum
    }

    double getVariance() {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        if(temp>0)
        return temp/(size-1);
        return temp
    }

    double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double median() {
        Arrays.sort(data);

        if (data.length % 2 == 0) {
            return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
        }
        return data[data.length / 2];
    }
}
