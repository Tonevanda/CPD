using System;

class MatrixMultiplication
{
    static void Main(string[] args)
    {
        Console.WriteLine("1.Multiplication");
        Console.WriteLine("2.Line Multiplication");
        Console.WriteLine("3.Block Multiplication");

        int choice = Convert.ToInt32(Console.ReadLine());

        switch(choice){
            case 1:
                OnMult();
                break;
            case 2:
                OnLineMult();
                break;
            case 3:
                OnBlockMult();
                break;
            default:
                Console.WriteLine("Invalid choice");
                break;
        }
    }

    static void OnMult(){
        Console.Write("Enter the size of the matrix: ");
        int size = Convert.ToInt32(Console.ReadLine());

        double[] pha = new double[size * size];
        double[] phb = new double[size * size];
        double[] phc = new double[size * size];

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                pha[i*size + j] = (double)1.0;
            }
        }

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                phb[i*size + j] = (double)(i+1);
            }
        }

        DateTime start = DateTime.Now;

        double temp;

        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                temp = 0;
                for (int k = 0; k < size; k++){
                    temp += pha[i*size + k] * phb[k*size + j];
                }
                phc[i*size + j] = temp;
            }
        }

        DateTime end = DateTime.Now;
        TimeSpan duration = end - start;

        Console.WriteLine("Time taken: " + duration.TotalSeconds + " seconds");
        Console.WriteLine("Result matrix:");
        for(int i = 0; i<1; i++){	
            for(int j = 0; j<Math.Min(10,size); j++)
                Console.Write(phc[j] + " ");
        }
        Console.WriteLine();
    }

    static void OnLineMult(){
        Console.Write("Enter the size of the matrix: ");
        int size = Convert.ToInt32(Console.ReadLine());

        double[] pha = new double[size * size];
        double[] phb = new double[size * size];
        double[] phc = new double[size * size];

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                pha[i*size + j] = (double)1.0;
            }
        }

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                phb[i*size + j] = (double)(i+1);
            }
        }

        for (int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                phc[i*size + j] = (double)0.0;
            }
        }

        DateTime start = DateTime.Now;

        for (int i = 0; i < size; i++){
            for (int k = 0; k < size; k++){
                for (int j = 0; j < size; j++){
                    phc[i*size + j] += pha[i*size + k] * phb[k*size + j];
                }
            }
        }

        DateTime end = DateTime.Now;
        TimeSpan duration = end - start;

        Console.WriteLine("Time taken: " + duration.TotalSeconds + " seconds");
        Console.WriteLine("Result matrix:");
        for(int i = 0; i<1; i++){	
            for(int j = 0; j<Math.Min(10,size); j++)
                Console.Write(phc[j] + " ");
        }
        Console.WriteLine();
    }

    static void OnBlockMult(){
        Console.WriteLine("Enter the size of the matrix: ");
        Console.WriteLine("TODO");
    }
}
