import scipy.special
import scipy.optimize
import numpy as np


def easyerf(x,a,b,c, d):
    return a*scipy.special.erf((b * x) - c) + d


def pwerf(x, a1, a2, b, c1, c2, h):
	e = ( (c1 / b) + (c2 / b)) / 2
	returnarray = []
	for i in x:
		if i < e:
			returnarray.append(easyerf(i, a1, b, c1, h - a1))
		else:
			returnarray.append(easyerf(i, a2, b, c2, h + a2))
	return returnarray

# a = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238]
# b = [63, 67, 75, 68, 78, 63, 52, 50, 56, 67, 70, 77, 59, 69, 63, 53, 72, 71, 64, 72, 61, 68, 78, 81, 87, 69, 68, 83, 64, 82, 100, 92, 96, 125, 131, 166, 151, 161, 132, 120, 112, 154, 177, 188, 151, 172, 157, 111, 156, 124, 131, 156, 138, 114, 140, 112, 129, 204, 157, 157, 168, 115, 125, 144, 139, 178, 146, 128, 143, 168, 173, 195, 115, 101, 149, 169, 150, 135, 132, 149, 214, 216, 196, 176, 168, 220, 177, 216, 178, 208, 215, 204, 212, 193, 155, 192, 176, 202, 166, 149, 174, 129, 109, 152, 230, 150, 228, 173, 133, 164, 180, 152, 162, 179, 139, 165, 159, 124, 164, 177, 175, 201, 195, 169, 146, 138, 174, 135, 153, 119, 150, 152, 140, 171, 140, 107, 134, 156, 126, 102, 140, 157, 149, 142, 123, 110, 169, 157, 142, 126, 183, 175, 187, 185, 147, 207, 168, 134, 174, 131, 150, 112, 110, 93, 94, 110, 74, 77, 79, 71, 82, 83, 89, 62, 82, 79, 85, 84, 73, 89, 85, 65, 92, 62, 67, 80, 68, 72, 68, 63, 70, 66, 70, 69, 62, 54, 67, 54, 58, 77, 70, 60, 56, 65, 57, 61, 63, 55, 60, 57, 75, 60, 62, 61, 55, 66, 58, 66, 55, 72, 61, 61, 53, 65, 68, 67, 53, 70, 54, 62, 63, 60, 66, 61, 63, 46, 54, 57]

import sys

f = open("python/intensitiesstring.txt", "r")

## f = open("intensitiesstring.txt", "r")
u1 = f.readline()
u2 = f.readline()
u3 = f.readline()

a = u1.split(",") ## indexes
a = np.array([float(i) for i in a])
b = u2.split(",") ## intensities
b = np.array([float(i) for i in b])
cm = float(u3)

#print(len(a), len(b))

## define guesses for curve fit


# import matplotlib.pyplot as plt
# plt.plot(a, b, 'o')

# plt.show()


minindex = np.min(a)
maxindex = np.max(a)
minint = np.min(b)
maxint = np.max(b)
medianindex = np.median(a)

arg1 = np.ptp(b) ## range
arg2 = -np.ptp(b)
arg3 = 1
arg4 = (minindex + medianindex) / 2
arg5 = (maxindex + medianindex) / 2
arg6 = np.average(b)
arg7 = (minindex + maxindex) / 2

## curve fit
try:
    p, c = scipy.optimize.curve_fit(pwerf, a, b, [arg1, arg2, arg3, arg4, arg5, arg6])
except Exception as e:
    print(e)

## calculate r squared values
residuals = b - np.array(pwerf(a, p[0], p[1], p[2], p[3], p[4], p[5]))
ss_res = np.sum(residuals**2)
ss_tot = np.sum((b-np.mean(b))**2)
r_squared = 1 - (ss_res / ss_tot)

## output r squared and length
print(r_squared)
print(np.abs(p[4]/p[2] - p[3]/p[2]))
print(int(np.abs(p[4]/p[2])))
print(int(np.abs(p[3]/p[2])))


