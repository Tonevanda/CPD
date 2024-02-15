import time
import numpy as np

if __name__ == "__main__":
    size = int(input("Enter the size of the matrix: "))
    start = time.time()
    matrix1 = [[1 for _ in range(size)] for _ in range(size)]
    matrix2 = [[i+1 for _ in range(size)] for i in range(size)]
    print(np.matmul(matrix1, matrix2))
    end = time.time()
    print("Time taken: ", end - start)