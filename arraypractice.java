import java.util.Random;

public class arraypractice{
    public static void main(String[] args){
        int arr[]; //declaring array
         // or int[] intArray

        arr = new int[10];
        // or int[] intArray = new int[10]
        
        //int x = 10;

        for (int i = 0; i < arr.length; i++){
            int rand = randint(5, 10);
            //System.out.println(randint(5, 10));

            arr[i] = rand;
            System.out.println("arr[" + i + "]: " + arr[i]);
            
            
        }

    }

    private static int randint (int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min; 
    }
}
