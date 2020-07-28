import math
import argparse
parser = argparse.ArgumentParser()
parser.add_argument('TrainingFile')
parser.add_argument('TestFile')
args = parser.parse_args()
#print(args)
trainFilePath = args.TrainingFile
testFilePath = args.TestFile
#print(trainFilePath)
#print(testFilePath)

trainFile = open(trainFilePath, "r")
testFile = open(testFilePath,"r")

k = 1 ##NN k value
print("k=",k)
trainSet = []
testSet = []
dimRanges = []

#trainFile = open("wine-training", "r")
#testFile = open("wine-test","r")
trainTxt = trainFile.readlines()
testTxt = testFile.readlines()

#print(trainTxt)
#print(testTxt)

dimensions = trainTxt[0].split()
dimNum = len(dimensions)

def GetData(file):
	dataList = []
#	print(len(file))
	for line in file[1:]:
#		print(line)
		instance = list(map(float,line.split()))
		checkDim = len(instance)
		if not checkDim == dimNum:
			print(instance)
			raise ValueError("line does not have right number of dimensions")
		dataList.append(instance)
	return dataList
		
def FindNormDist(instA, instB):	
	sum = 0
#	print("Finding distance between:")
	#print(instA, instB)
	for i in range(dimNum -1 ): #for each dimension	except class		
#		print("dimension:", i)
		dimDistSqrd = (instA[i] - instB[i])**2
#		print("dimDistSqrd", dimDistSqrd)
		normDimDistSqrd = dimDistSqrd / float((dimRanges[i])**2)
#		print("normDimDistSqrd", normDimDistSqrd)
		sum = sum + normDimDistSqrd
#		print("sum", sum)
	dist = sum**0.5
	
	return dist


	
trainSet = GetData(trainTxt)
testSet = GetData(testTxt)

print("Dimesions",dimensions)
#print("trainSet:")
#print(trainSet)
#print("TestSet:")
#print(testSet)

#find range of each dimension and store in list dimRanges
for d in range(dimNum):
	#find min and max of dimension checking train and test
	dimMin = trainSet[0][d]
#	print("Min init: " + str(dimMin))
	dimMax = trainSet[0][d]
#	print("Max init: " + str(dimMax))
	for trainInst in trainSet: 
		if trainInst[d] > dimMax:
			dimMax = trainInst[d]
#			print("new max: " + str(dimMax))
		if trainInst[d] < dimMin:
			dimMin = trainInst[d]
#			print("new min: " + str(dimMin))
	for testInst in testSet: 
		if testInst[d] > dimMax:
			dimMax = testInst[d]
#			print("new max: " + str(dimMax))
		if testInst[d] < dimMin:
			dimMin = testInst[d]
#			print("new min: " + str(dimMin))
	dimRanges.append(dimMax - dimMin)
#	print(len(dimRanges))
report = open("report.txt", "w+")
report.write("Actual Predicted-Class\n")
print("Dimension Ranges",dimRanges)
correctCount = 0
incorrectCount = 0
for testInst in testSet:
	distsToTrain = []
	for trainInst in trainSet:		
		classVal = trainInst[-1]
		dist = FindNormDist(testInst, trainInst)
		#print("dist:", dist)
		#print("")
		distsToTrain.append((dist, classVal))
#	print("unsorted dist from", testInst)
#	print(distsToTrain)
	print("")
	print("K NN's of ", testInst)
	distsToTrain.sort(key=lambda tup: tup[0])
	#print(distsToTrain)
	distsToTrain = distsToTrain[:k]
	#print("trimmed")
	print("(dist, class)")
	print(distsToTrain)
	classCount = {}
	for dist in distsToTrain:
		if dist[1] in classCount:
			classCount[dist[1]] += 1
		else:
			classCount[dist[1]] = 1
#	print("classCount",classCount)
	aux = [(classCount[key], key) for key in classCount]
	aux.sort(reverse = True)
	print("sorted classCount",aux)
	predictedClass = aux[0][1]
	print("predicted class", predictedClass)
	report.write(str(int(testInst[-1])) +"	" + str(int(predictedClass))+"\n")
	if testInst[-1] == predictedClass:
		print("Correct!")
		correctCount += 1
	else: 
		print("Incorrect")
		incorrectCount +=1

report.write("Number of Correct Predictions: " + str(correctCount)+"\n")
report.write("Number of Incorrect Predictions: " + str(incorrectCount)+"\n")
report.write("Accuracy " + str(float(correctCount)/(incorrectCount+correctCount)))
print("Num correct predictions:", correctCount)
print("Num incorrect predictions:", incorrectCount)
print("accuracy: ", float(correctCount)/(incorrectCount+correctCount))	
	

	


