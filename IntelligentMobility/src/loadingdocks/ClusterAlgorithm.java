package loadingdocks;

import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;

public interface ClusterAlgorithm {

//    void cluster(DoubleMatrix2D data, int clusters);

    DoubleMatrix2D getPartition();

	void cluster(Map<Integer, User> users, int clusters);
}