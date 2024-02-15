import time

def matrix_multiplication(matrix1, matrix2):
    rows1 = len(matrix1)
    cols1 = len(matrix1[0])
    rows2 = len(matrix2)
    cols2 = len(matrix2[0])

    if cols1 != rows2:
        return None

    result = [[0 for _ in range(cols2)] for _ in range(rows1)]

    for i in range(rows1):
        for j in range(cols2):
            for k in range(cols1):
                result[i][j] += matrix1[i][k] * matrix2[k][j]

    return result

if __name__ == "__main__":
    start = time.time()
    size = int(input("Enter the size of the matrix: "))
    matrix1 = [[1 for _ in range(size)] for _ in range(size)]
    matrix2 = [[i+1 for _ in range(size)] for i in range(size)]
    print(matrix_multiplication(matrix1, matrix2))
    end = time.time()
    print("Time taken: ", end - start)