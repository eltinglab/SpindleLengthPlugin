# import scipy.optimize
# import scipy.special
# import numpy as np
import sys

import warnings
warnings.filterwarnings("ignore")


# # ## parse command line arguments


indexes = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237]
intensities = [0.050938000000000004, 0.049611, 0.054938, 0.047958, 0.047284999999999994, 0.067245, 0.051958, 0.054245, 0.050263999999999996, 0.048591, 0.044958, 0.056263999999999995, 0.051632, 0.055284999999999994, 0.057938, 0.056245, 0.047284999999999994, 0.056284999999999995, 0.046285, 0.037305, 0.045264, 0.044958, 0.045264, 0.059591, 0.049284999999999995, 0.042631999999999996, 0.046938, 0.051938000000000005, 0.039958, 0.058571, 0.049611, 0.051611000000000004, 0.047958, 0.053958, 0.059611000000000004, 0.043632, 0.044611000000000005, 0.05461100000000001, 0.04661100000000001, 0.047263999999999994, 0.061611000000000006, 0.050631999999999996, 0.057284999999999996, 0.048611, 0.057263999999999995, 0.047938, 0.062245, 0.064938, 0.063917, 0.058611, 0.06293800000000001, 0.064285, 0.07628499999999999, 0.07022400000000001, 0.073877, 0.094509, 0.12653, 0.10389799999999999, 0.11418299999999999, 0.137816, 0.135143, 0.14553, 0.122224, 0.11053, 0.07955, 0.133877, 0.114224, 0.13759100000000002, 0.116877, 0.14049, 0.100285, 0.141203, 0.117143, 0.13026400000000002, 0.159143, 0.17383600000000002, 0.14953, 0.147509, 0.151796, 0.14412200000000003, 0.179082, 0.123836, 0.182428, 0.13655, 0.197469, 0.163143, 0.168101, 0.181469, 0.189509, 0.202694, 0.164836, 0.15253, 0.167469, 0.151203, 0.131143, 0.178082, 0.20108199999999998, 0.166877, 0.138877, 0.17050900000000002, 0.164041, 0.16373500000000002, 0.145469, 0.13883600000000001, 0.148816, 0.166203, 0.13655, 0.13622399999999998, 0.13222399999999998, 0.122836, 0.128143, 0.11253, 0.104224, 0.14450900000000003, 0.11283599999999999, 0.114224, 0.12922399999999998, 0.115571, 0.125509, 0.130203, 0.120143, 0.127877, 0.126469, 0.127203, 0.128816, 0.106224, 0.120877, 0.116836, 0.111856, 0.126877, 0.116591, 0.10285599999999999, 0.125183, 0.129203, 0.10949, 0.151509, 0.152796, 0.154122, 0.170224, 0.180101, 0.17312200000000003, 0.179408, 0.15822399999999998, 0.188143, 0.149101, 0.128856, 0.157143, 0.17146899999999998, 0.164469, 0.142898, 0.127224, 0.070938, 0.087591, 0.091591, 0.098836, 0.072571, 0.104203, 0.06524500000000001, 0.08722400000000001, 0.073938, 0.080938, 0.08153, 0.075245, 0.056938, 0.057611, 0.048263999999999994, 0.056611, 0.08455, 0.051285, 0.06024500000000001, 0.054938, 0.058285, 0.06524500000000001, 0.046285, 0.053938, 0.07022400000000001, 0.062958, 0.061591, 0.045285, 0.068938, 0.059285, 0.051938000000000005, 0.050957999999999996, 0.041611, 0.047958, 0.052591, 0.048958, 0.038958, 0.040284999999999994, 0.048938, 0.051632, 0.048958, 0.045958, 0.045591, 0.046285, 0.040958, 0.048631999999999995, 0.059958, 0.055570999999999995, 0.053611000000000006, 0.043611000000000004, 0.053938, 0.037958, 0.052958, 0.041957999999999995, 0.051611000000000004, 0.049611, 0.050284999999999996, 0.041631999999999995, 0.047938, 0.046958, 0.051285, 0.041284999999999995, 0.036611000000000005, 0.039284999999999994, 0.043632, 0.052611000000000005, 0.043632, 0.037958, 0.041631999999999995, 0.049284999999999995, 0.036632, 0.037958, 0.042957999999999996, 0.039284999999999994, 0.040958, 0.050631999999999996, 0.047958, 0.050284999999999996, 0.038958, 0.043632, 0.048611, 0.055938, 0.050284999999999996, 0.044285, 0.034631999999999996, 0.039632]
# ## fit piecewise error function and find ends/length
# par4, con4 = scipy.optimize.curve_fit(pwerf, indexes, intensities, [10, 10, 1, 50, 170, 25,120])

# minindex = par4[4]/par4[2]

# maxindex = par4[3]/par4[2]

# length = np.abs(maxindex - minindex)

# print(length)

import scipy.optimize
import scipy.special
import numpy as np

def fun(x, a, b):
    return (a * x + b)

# indexes = sys.argv[1].split(",")
# intensities = sys.argv[2].split(",")

# indexes = [float(i) for i in indexes]
# intensities = [float(i)/1000 for i in intensities]

## modifiable error function
def easyerf(x,a,b,c, d):
    return a*scipy.special.erf((b * x) - c) + d

## piecewise error function
def pwerf(x, a1, a2, b, c1, c2, h, e):
    returnarray = []
    for i in x:
        if i < e:
            returnarray.append(easyerf(i, a1, b, c1, h - a1))
        else:
            returnarray.append(easyerf(i, a2, b, c2, h + a2))
    return returnarray


array = np.linspace(-5, 10, 15)
array2 = np.linspace(10, 25, 15)
array3 = [easyerf(i, 4, 1, 1, 1) for i in array]
array4 = [easyerf(i, -4, 1, 15, 1) for i in array2]
array5 = np.concatenate((array,array2)) # x values
array6 = np.concatenate((array3,array4)) # y values
pguess7 = [10, -4, 17, 1, 15, 1, 10]

t = np.linspace(0, 10, 10)
r = [2 * i + 5 for i in t]

par, con = scipy.optimize.curve_fit(fun, t, r, [1, 10])

par3, con3 = scipy.optimize.curve_fit(pwerf, array5, array6, p0 = pguess7)

print("got here")

## fit piecewise error function and find ends/length
par4, con4 = scipy.optimize.curve_fit(pwerf, indexes, intensities, [10, 1, 1, 5, 17, 25,120])
print(par4)
print("now got here")


