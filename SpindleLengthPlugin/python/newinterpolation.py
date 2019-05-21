import sys
import numpy as np
import scipy.stats


## uncomment below to read the values from interpolationdatanorm.csv and copy/paste them into arrays below.
# import csv

# # maxima = []
# # minima = []
# # skews = []
# # stdevs = []
# # means  = []
# # thresholds = []
# # print("3")
# with open('interpolationdatanorm.csv') as file:
#     print("3")
#     reader = csv.reader(file, delimiter=',')
#     line_count = 0
#     for row in reader:
#         if line_count == 0:
#             #print(f'Column names are {", ".join(row)}')
#             line_count += 1
#         else:
#             # maxima.append(float(row[0]))
#             # minima.append(float(row[1]))
#             # means.append(float(row[2]))
#             # stdevs.append(float(row[3]))
#             # skews.append(float(row[4]))
#             # thresholds.append(float(row[5]))
#             print(row[5], end = ',')
#             line_count += 1

## builds interpolation scheme based on values from interpolationdata2.csv
maxima = [65535,65535,65535,65535,65535,53798,65535,65535,43692,58391,65535,65535,46574,64390,65535,64491,53003,52598,65535,53935,47509,65535,65535,65535,65535,58010,65535,65535,65535,49906,52944,65535,65535,65535,65535,65535,58749,65535,65535,47332,49841,65535,65535,37809,47332,34906,65535,45170,60657,38343]
minima = [0,0,522,0,424,0,0,0,0,1809,0,762,11045,0,1771,0,0,0,0,4957,0,0,0,261,0,1333,0,1484,0,5416,6192,848,0,1771,1428,2095,848,0,0,0,0,0,1771,0,0,0,0,0,2332,0]
means = [8359.091870118185,9211.791866028709,17395.898484848483,21561.722307692307,16370.056883640553,7939.780006543165,19720.947245753217,7797.5861857440805,27764.09385504908,15920.248310314428,16881.844470046082,15428.251101968852,20598.80982142857,3136.4881803188564,11695.699462365592,17928.305587121213,22206.59441287879,20477.232430875578,20428.2241025641,9954.34482142857,4103.5480248174035,3667.606298594204,19236.803370534715,17452.433143939394,22264.40282051282,16423.590655304142,11936.444158181,15838.13090437788,3708.6918243933087,9117.833214285714,10685.74875,16227.574596774193,19212.692797274885,12261.436559139785,13857.798413164855,13328.438142815163,17798.002880184333,16672.668202764977,10539.733713654767,25682.07816771996,21936.466301843317,13619.947204301076,11878.967096774193,4943.8915416633945,25607.436959347408,5343.731720725674,20741.583076923078,3702.7341720629047,25704.720075757577,16020.071284562211,4903.758265923192,38094.893231516835]
stdevs = [3203.2845254896915,3165.0373219253115,5635.726039252199,10308.975689028932,6345.705241740545,3053.6638713350976,5628.695333487836,2877.217848351087,7349.19334937749,6216.052450616476,6563.357586954415,5596.564549416376,9036.020055356032,6252.078987958935,6264.786996928262,5381.704101373608,6480.6721694135995,7219.940822768725,9184.66252898366,5854.4712118045445,7831.132427676738,7190.203384635951,6391.611951807942,5300.909462499919,10459.825696961329,6163.572912153082,4189.582212493437,6349.4137076033,7220.839012852604,5821.932742781236,6856.868922832721,6665.421985711297,6378.596485416678,7802.245036006575,5359.04947774698,5247.546292048914,6710.2063086379685,6530.353283596086,4118.1588866549755,6892.662731972036,7555.741527586722,6046.123093986982,6522.003515807677,9298.375850800601,7129.879476639133,9875.423101313196,8687.989477494028,3271.8430569847055,7464.587747137186,6527.2432916757525,9165.18978851043,5156.666454705101]
skews = [4.080247448270712,2.386595236812479,2.722243965539885,2.1954427627820743,1.550058517116341,2.7138245394345017,1.3972168876770343,5.96911319295972,0.4693243759102302,2.4438282713228188,1.9054003781452902,2.6908868633000176,1.5900153954119236,2.767423988931333,2.9012146282909446,1.6980965544920705,1.2321635440065466,0.6309492216563083,1.6535363794752338,4.73384822105012,2.0261867818540584,2.4108182925606543,3.1532003700719993,2.059723839839883,2.110959784015867,2.6525285941840826,2.40392138995444,1.3439194711466278,2.2675751783575966,5.203962518697347,4.1118974459813575,1.8624965241515585,3.177438636075001,3.6162700300517003,3.1675941168164283,3.644619368886411,0.7014572186595038,1.8598494070941851,2.14375993970438,0.4320075921686075,0.5358986303462676,1.8835617442146797,2.992200418578219,1.8305218066769307,0.6284219278737576,1.6779960530248352,1.493893417886505,9.524586650127583,0.8835020236742719,0.76661618134116,1.7762156057990293,-4.7524690225433055]
thresholds = [25000,24000,40000,47000,45000,26000,46000,30000,47000,42000,47000,46000,48000,39000,30000,39000,39000,43000,42000,34000,40000,44000,60000,49000,63000,49000,27000,50000,48000,36000,36000,43000,53000,32000,42000,39000,40000,43000,25000,45000,44000,36000,33000,41000,45000,42000,39000,25000,45000,35000,38000,45000]

import scipy.interpolate

hi = scipy.interpolate.Rbf(stdevs, means, skews, thresholds, function = "linear")

## takes in array of intensities as comma separated string command line argument
## outputs interpolated intensity threshold value


hist = sys.argv[1].split(",")

hist = [float(i) for i in hist]

def getThreshold(oned):	
	maxx = np.max(oned)
	minn = np.min(oned)
	stdev = np.std(oned)
	mean = np.average(oned)
	skew = scipy.stats.skew(oned)

	thresholdcalc = hi(stdev, mean, skew)

	# print("max: ", maxx)
	# print("min: ", minn)
	# print("stdev: ", stdev)
	# print("mean: ", mean)
	# print("skew: ", skew)

	return thresholdcalc

print((getThreshold(hist)))

# print(hi(3914.677381574009, 21101.920097188475, 2.2126251338786282))

