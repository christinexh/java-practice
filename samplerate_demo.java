import java.util.Random;
import java.util.Arrays;

public class samplerate_demo{
    public static void main(String[] args){
        int arr[]; //declaring array
         // or int[] intArray

        arr = new int[10];
        // or int[] intArray = new int[10]
        int i = 0;        //int x = 10;
        int k;
        int rand;
        int sum = 0;
        
        for( i = 1; (5*i - 1) < arr.length; i++){
            int j = 5*i - 1;
            System.out.println("" + j);
            
            for (k = j - 4; k <= j; k++){
                rand = randint(5,10);
                arr[k] = rand;
                System.out.println("arr[" + k + "]:" + arr[k]);
                                             
                }
            
            System.out.println(Arrays.toString(arr));
            
            for (k = j - 4; k <= j; k++){
                sum += arr[k];
                //System.out.println("" + avg);
            }

            System.out.println("sum = " + sum);

        }

    }

    private static int randint (int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min; 
    }
}
