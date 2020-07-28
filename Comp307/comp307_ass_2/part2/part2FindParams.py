import sklearn
import argparse
from sklearn.neural_network import MLPClassifier
from sklearn import preprocessing

#parser = argparse.ArgumentParser()
#parser.add_argument('TrainingFile')
#parser.add_argument('TestFile')
#args = parser.parse_args()
#print(args)
#trainFilePath = args.TrainingFile
#testFilePath = args.TestFile
trainFile = open("part2data/wine_training", "r")
testFile = open("part2data/wine_test","r")
#trainFile = open(trainFilePath, "r")
#testFile = open(testFilePath,"r")


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

def trainAndScore(sol, al, layerSize, loopAmm):
	global trainX, trainY, testX,testY
	totalScore = 0
	for i in range(0, loopAmm):
		clf = MLPClassifier(solver=sol, alpha=al, hidden_layer_sizes=layerSize, max_iter=200000000, random_state=i)
		clf.fit(trainX, trainY)
		score = clf.score(testX,testY)
		#print(i, score)
		totalScore += score
	return totalScore/loopAmm
		

trainX,trainY = GetData(trainFile)
testX,testY= GetData(testFile) 
scaler = preprocessing.StandardScaler().fit(trainX)
SCALE = True
if (SCALE):
	trainX = scaler.transform(trainX)
	testX = scaler.transform(testX)

alpha = 1e-5
highestScore = 0
bestAlpha = 0
bestLayer = 0

for a in range(3,8):
	print("alpha:", a)
	al = alpha * float(10**a)
	for l in range(2, 14):		
		print(al, l)
		score = trainAndScore('lbfgs', al, l, 10)
		print(score)
		if (score > highestScore) :
			bestAlpha = al
			bestLayer = l
			highestScore = score
		print("----")
		
print(bestAlpha, bestLayer, highestScore)
