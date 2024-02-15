import time

def OnMult():
    size = int(input("Enter the size of the matrix: "))


    pha = [1 for _ in range(size*size)]
    phb = [(i+1) for i in range(size) for j in range(size)]
    phc = [0 for _ in range(size*size)]

    start = time.time()

    for i in range(size):
        for j in range(size):
            temp = 0
            for k in range(size):
                temp += pha[i*size+k] * phb[k*size+j]
            phc[i*size+j] = temp

    end = time.time()
    print("Time taken: ", end - start)
    print("Result matrix: ", phc[:10])

# TODO - Use single list instead of list of lists
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

if __name__ == "__main__":
    print("1. Multiplication")
    print("2. Line Multiplication")
    print("3. Block Multiplication")
    t = int(input("Enter the type of computation: "))
    match t:
        case 1:
            OnMult()
        case 2:
            OnMultLine()
        case _:
            print("Invalid input")