import scipy.special
import scipy.optimize
import numpy as np


def easyerf(x,a,b,c, d):
    return a*scipy.special.erf((b * x) - c) + d


def pwerf(x, a1, a2, b, c1, c2, h, e):
    returnarray = []
    for i in x:
        if i < e:
            returnarray.append(easyerf(i, a1, b, c1, h - a1))
        else:
            returnarray.append(easyerf(i, a2, b, c2, h + a2))
    return returnarray

import sys
a = sys.argv[1].split(",")
a = np.array([float(i) for i in a])
b = sys.argv[2].split(",")
b = np.array([float(i) for i in b])

#print(len(a), len(b))

try:
    p, c = scipy.optimize.curve_fit(pwerf, a, b, [200, 1, 1, 50, 17, 25,120])
except Exception as e:
    print(e)

print(np.abs(p[4]/p[2] - p[3]/p[2]))
