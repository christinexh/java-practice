import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;

public class samplerate_arraylist{
    public static void main(String[] args){

        int n = 5;
        ArrayList<Integer> arrli = new ArrayList<Integer>(n);
        int i;
        int k;
        int rand;
        double sum = 0;
        double avg = 0;
        
        for(k = 1; k <= 2; k++){

            // creates arraylist
            for(i = 0; i < n; i++){
                rand = randint(5,10);
                arrli.add(rand);

                int e = arrli.get(i);
                sum += e;

                avg = sum / n;
                
            }

            //displays arralist
            System.out.println(arrli);

            //displays sum of elements
            System.out.println("Sum #" + k + " = " + sum);

            //displays average
            System.out.println("Average #" + k + " = " + avg);

            //clears arraylist
            arrli.clear();

            //resets sum to zero
            if(sum != 0){
                sum = 0;
            }
                       
        }

    }

    private static int randint (int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min; 
    }
}