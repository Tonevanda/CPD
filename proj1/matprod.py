import time
import numpy as np

def OnMultNumpy():
    size = int(input("Enter the size of the matrix: "))
    start = time.time()
    matrix1 = [[1 for _ in range(size)] for _ in range(size)]
    matrix2 = [[i+1 for _ in range(size)] for i in range(size)]
    np.matmul(matrix1, matrix2)
    end = time.time()
    print("Time taken: ", end - start)

def OnMultLine():
    size = int(input("Enter the size of the matrix: "))
    start = time.time()
    matrix1 = [[1 for _ in range(size)] for _ in range(size)]
    matrix2 = [[i+1 for _ in range(size)] for i in range(size)]
    result = [[0 for _ in range(size)] for _ in range(size)]
    for i in range(size):
        for j in range(size):
            for k in range(size):
                result[i][j] += matrix1[i][k] * matrix2[k][j]
    end = time.time()
    print("Time taken: ", end - start)

def OnMultBlock():
    size = int(input("Enter the size of the matrix: "))
    block_size = int(input("Enter the block size: "))
    start = time.time()
    matrix1 = [[1 for _ in range(size)] for _ in range(size)]
    matrix2 = [[i+1 for _ in range(size)] for i in range(size)]
    result = [[0 for _ in range(size)] for _ in range(size)]
    for i in range(0, size, block_size):
        for j in range(0, size, block_size):
            for k in range(0, size, block_size):
                for ii in range(i, min(i+block_size, size)):
                    for jj in range(j, min(j+block_size, size)):
                        for kk in range(k, min(k+block_size, size)):
                            result[ii][jj] += matrix1[ii][kk] * matrix2[kk][jj]
    end = time.time()
    print("Time taken: ", end - start)

if __name__ == "__main__":
    print("1. Multiplication")
    print("2. Line Multiplication")
    print("3. Block Multiplication")
    t = int(input("Enter the type of computation: "))
    match t:
        case 1:
            OnMultNumpy()
        case 2:
            OnMultLine()
        case 3:
            OnMultBlock()
        case _:
            print("Invalid input")