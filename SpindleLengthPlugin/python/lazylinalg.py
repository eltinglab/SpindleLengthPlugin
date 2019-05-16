import numpy as np 
import scipy.linalg
import sys

## takes in moment of inertia tensor matrix as comma seperated string command line argument
## outputs principal eigenvector

a = sys.argv[1].split(",")
a = [float(i) for i in a]

matrix = np.array(([a[0], a[1]], [a[2], a[3]]))

w,v = scipy.linalg.eig(matrix)

if np.abs(w[0]) > np.abs(w[1]): ## we want the eigenvector corresponding to the larger eigenvalue
    mainvector = v[:,0]
else:
    mainvector = v[:,1]


print(mainvector[0])
print(mainvector[1])