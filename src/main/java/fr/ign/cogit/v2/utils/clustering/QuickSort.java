package fr.ign.cogit.v2.utils.clustering;

public class QuickSort {

    public void Sort(int[] numbers, int[] indices, int low, int high) {
        int i = low, j = high;       
        // Get the pivot element from the middle of the list
        int pivot = numbers[low + (high-low)/2];

        // Divide into two lists
        while (i <= j) {
          // If the current value from the left list is smaller then the pivot
          // element then get the next element from the left list
          while (numbers[i] < pivot) {
            i++;
          }
          // If the current value from the right list is larger then the pivot
          // element then get the next element from the right list
          while (numbers[j] > pivot) {
            j--;
          }

          // If we have found a values in the left list which is larger then
          // the pivot element and if we have found a value in the right list
          // which is smaller then the pivot element then we exchange the
          // values.
          // As we are done we can increase i and j
          if (i <= j) {
            exchange(numbers, indices, i, j);
            i++;
            j--;
          }
        }
        // Recursion
        if (low < j)
            Sort(numbers, indices,low, j);
        if (i < high)
            Sort(numbers, indices,i, high);
    }
    
    private void exchange(int[] numbers, int[] indices, int i, int j) {
        int temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
        
        int temp2 = indices[i];
        indices[i] = indices[j];
        indices[j] = temp2;
      }
    

}
