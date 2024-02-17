using System;

class MatrixMultiplication
{
    static void Main(string[] args)
    {
        Console.WriteLine("1.Multiplication");
        Console.WriteLine("2.Line Multiplication");
        Console.WriteLine("Selection?: ");

        int choice = Convert.ToInt32(Console.ReadLine());

        Console.WriteLine("Dimensions: lins=cols ? ");
        int lins = Convert.ToInt32(Console.ReadLine());
        int cols = lins;

        switch(choice){
            case 1:
                OnMult(lins,cols);
                break;
            case 2:
                OnLineMult(lins,cols);
                break;
            default:
                Console.WriteLine("Invalid choice");
                break;
        }
    }

    static void OnMult(int lins, int cols){

        double[] pha = new double[lins * cols];
        double[] phb = new double[lins * cols];
        double[] phc = new double[lins * cols];

        for (int i = 0; i < lins; i++){
            for(int j = 0; j < cols; j++){
                pha[i*lins + j] = (double)1.0;
            }
        }

        for (int i = 0; i < lins; i++){
            for(int j = 0; j < cols; j++){
                phb[i*cols + j] = (double)(i+1);
            }
        }

        DateTime start = DateTime.Now;

        double temp;

        for (int i = 0; i < lins; i++){
            for (int j = 0; j < cols; j++){
                temp = 0;
                for (int k = 0; k < lins; k++){
                    temp += pha[i*lins + k] * phb[k*cols + j];
                }
                phc[i*lins + j] = temp;
            }
        }

        DateTime end = DateTime.Now;
        TimeSpan duration = end - start;

        Console.WriteLine("Time taken: " + duration.TotalSeconds + " seconds");
        Console.WriteLine("Result matrix:");
        for(int i = 0; i<1; i++){	
            for(int j = 0; j<Math.Min(10,cols); j++)
                Console.Write(phc[j] + " ");
        }
        Console.WriteLine();
    }

    static void OnLineMult(int lins, int cols){

        double[] pha = new double[lins * cols];
        double[] phb = new double[lins * cols];
        double[] phc = new double[lins * cols];

        for (int i = 0; i < lins; i++){
            for(int j = 0; j < cols; j++){
                pha[i*lins + j] = (double)1.0;
            }
        }

        for (int i = 0; i < lins; i++){
            for(int j = 0; j < cols; j++){
                phb[i*cols + j] = (double)(i+1);
            }
        }

        for (int i = 0; i < lins; i++){
            for(int j = 0; j < cols; j++){
                phc[i*lins + j] = (double)0.0;
            }
        }

        DateTime start = DateTime.Now;

        for (int i = 0; i < lins; i++){
            for (int k = 0; k < lins; k++){
                for (int j = 0; j < cols; j++){
                    phc[i*lins + j] += pha[i*lins + k] * phb[k*cols + j];
                }
            }
        }

        DateTime end = DateTime.Now;
        TimeSpan duration = end - start;

        Console.WriteLine("Time taken: " + duration.TotalSeconds + " seconds");
        Console.WriteLine("Result matrix:");
        for(int i = 0; i<1; i++){	
            for(int j = 0; j<Math.Min(10,cols); j++)
                Console.Write(phc[j] + " ");
        }
        Console.WriteLine();
    }
}
