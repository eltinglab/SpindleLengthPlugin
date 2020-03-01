import numpy as np 
import scipy.linalg
import sys

## takes in moment of inertia tensor matrix as comma seperated string command line argument
## outputs principal eigenvector

f = open("python/matrixstring.txt", "r")
u = f.readline()

a = u.split(",")
a = [float(i) for i in a]

matrix = np.array(([a[0], a[1]], [a[2], a[3]]))

w,v = scipy.linalg.eig(matrix)

if np.abs(w[0]) > np.abs(w[1]): ## we want the eigenvector corresponding to the larger eigenvalue
    mainvector = v[:,0]
    othervector = v[:,1]
else:
    mainvector = v[:,1]
    othervector = v[:,0]


print(mainvector[0])
print(mainvector[1])
print(othervector[0])
print(othervector[1])

