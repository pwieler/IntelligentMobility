package loadingdocks;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Statistic.VectorVectorFunction;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class KMedoids implements ClusterAlgorithm {
   
   private DoubleMatrix2D partition;
   private int maxIterations = 1000;
   private IntArrayList medoids;
   private VectorVectorFunction distanceMeasure = Statistic.EUCLID;
   private int clusters;

   public  KMedoids() {
   }

   @Override
   public void cluster(List<User> users, int clusters) {
	  this.clusters = clusters;
	  DoubleMatrix2D data = createMatrix(users);
	   
      int n = data.rows(); // Number of features
      int p = data.columns(); // Dimensions of features

      partition = new SparseDoubleMatrix2D(n, clusters);
      medoids = new IntArrayList(clusters);

      IntArrayList randomOrdering = new IntArrayList(n);
      for (int i = 0; i < n; ++i) {
         randomOrdering.setQuick(i, i);
      }

      // Choose the medoids by shuffling the data
      for (int i = 0; i < clusters; ++i) {
         // k is the index of the remaining possibilities
         UniformIntegerDistribution uniform = new UniformIntegerDistribution(i, clusters );
         int k = uniform.sample();

         // Swap x(i) and x(k)
         int medoid = randomOrdering.getQuick(k);
         randomOrdering.setQuick(k, i);                     
         medoids.setQuick(i, medoid);
      }

      boolean changedMedoid = true;

      // Begin the main loop of alternating optimization
      for (int itr = 0; itr < maxIterations && changedMedoid; ++itr) {
         // Get new partition matrix U by
         // assigning each object to the nearest medoid
         for (int i = 0; i < n; i++) {            
            double minDistance = Double.MAX_VALUE;
            int closestCluster = 0;

            for (int k = 0; k < clusters; k++) {
               // U = 1 for the closest medoid
               // U = 0 otherwise
               int medoid = medoids.getQuick(k);
               double distance = distanceMeasure.apply(data.viewRow(medoid), data.viewRow(i));
               if (distance < minDistance) {
                  minDistance = distance;
                  closestCluster = k;
               }
            }

            if (partition.getQuick(i, closestCluster) == 0) {

               for (int k = 0; k < clusters; k++) {
                  partition.setQuick(i, k, (k == closestCluster) ? 1 : 0);
               }
            }
         }

         // Try to find a better set of medoids
         changedMedoid = false;
         for (int k = 0; k < clusters; k++) {

            // For each non-medoid in the cluster
            int medoid = medoids.getQuick(k);
            for (int i = 0; i < n; ++i) {
               int bestMedoid = medoid;
               double lowestCostDelta = 0;
               if (i != medoid && partition.getQuick(i, k) > 0) {
                  // Calculate the change in cost by swapping this configuration
                  int costDelta = 0;
                  for (int j = 0; j < n; ++j) {
                     if (partition.getQuick(j, k) > 0) {                        
                        double oldDistance = distanceMeasure.apply(data.viewRow(medoid), data.viewRow(j));
                        double newDistance = distanceMeasure.apply(data.viewRow(i), data.viewRow(j));
                        costDelta += newDistance - oldDistance;
                     }
                  }

                  if (costDelta < lowestCostDelta) {
                     bestMedoid = i;
                     lowestCostDelta = costDelta;
                  }

                  if (bestMedoid != medoid) {
                     medoids.setQuick(k, bestMedoid);
                     changedMedoid = true;
                  }
               }
            }
         }
      }
   }
   
   private DoubleMatrix2D createMatrix(List<User> users) {
	   double[][] values = new double[users.size()][4];
	   for(int i = 0; i <values.length;i++) {
			   values[i][0] = users.get(i).point.getX();
			   values[i][1] = users.get(i).point.getY();
			   values[i][2] = users.get(i).getTarget_position().getX();
			   values[i][3] = users.get(i).getTarget_position().getY();
	   }
	   
	   DoubleMatrix2D matrix = DoubleFactory2D.dense.make(values);
	   
	   return matrix.assign(values);
   }

   public  IntArrayList getMedoids() {
      return medoids;
   }

   @Override
   public  DoubleMatrix2D getPartition() {
      return partition;
   }

   public  int getMaxIterations() {
      return maxIterations;
   }

   public  void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }


   public  VectorVectorFunction getDistanceMeasure() {
      return distanceMeasure;
   }

   public  void setDistanceMeasure(VectorVectorFunction distanceMeasure) {
      this.distanceMeasure = distanceMeasure;
   }
   
   public List<User> userDefineCluster(List<User> users) {
	   for(int i=0; i<users.size(); i++) {
		   for(int j=0;j<clusters;j++) {
			   if(partition.get(i,j)==1)
				   users.get(i).setCluster(j+1);
		   }
	   }
	   return users;
   }
  

}