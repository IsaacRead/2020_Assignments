import sklearn
import argparse
from sklearn.neural_network import MLPClassifier
from sklearn import preprocessing


trainFile = open("part2data/wine_training", "r")
testFile = open("part2data/wine_test","r")

def GetData(file):
	fileText = file.readlines()
	x = []
	y = [] 
#	print(len(file))
	for line in fileText[1:]:
#		print(line)
		instance = list(map(float,line.split()))
		if (len(x) > 0):
			if (len(instance[:-1]) != len(x[0])):
				print(len(instance[:-1]))
				print(len(x[0]))
				raise Exception("somthing is wrong with data file")
		
		x.append(instance[:-1])
		
		y.append(instance[-1])	

	return x,y


trainX,trainY = GetData(trainFile)
testX,testY= GetData(testFile) 
scaler = preprocessing.StandardScaler().fit(trainX)
SCALE = True
if (SCALE):
	trainX = scaler.transform(trainX)
	testX = scaler.transform(testX)


testscores = 0
trainscores = 0
for i in range( 10):
	clf = MLPClassifier(solver='lbfgs', alpha=1, hidden_layer_sizes=2, max_iter=500, random_state=i)
	clf.fit(trainX, trainY)
	testScore = clf.score(testX,testY)
	testscores += testScore
	trainScore =  clf.score(trainX, trainY)
	trainscores += trainScore
	print("test", testScore )
	print("train", trainScore)

print("testAvg", testscores/10)
print("trainAvg", trainscores/10)


# print(score)
# correcttotal = 0
# for i in range(len(testX)):
# 	pred = clf.predict([testX[i]])
# 	if pred == testY[i]:
# 		correcttotal += 1
# print(correcttotal/len(testX))
