import argparse

def getData(fileName):
	file = open(fileName, "r")
	fileText = file.readlines()
	insts = []
#	print(len(file))
	for line in fileText:
#		print(line)
		instance = list(map(int,line.split()))
		if (len(insts) > 0):
			if (len(instance) != len(insts[0])):
				print(len(instance))
				print(len(insts[0]))
				raise Exception("somthing is wrong with data file")		
		insts.append(instance)
	return insts

def seperateByClass(labeledData):
	seperateData = dict()
	for i in range(len(labeledData)):
		values = labeledData[i][:-1]
		clss = labeledData[i][-1]
		#print(clss)		
		if clss not in seperateData:
			seperateData[clss] = list()
		seperateData[clss].append(values)
	return seperateData

def calcProbs(dataDict):
	probsForClass = dict()
	total = 0
	for key in dataDict:
		total+= len(dataDict[key])

	for key in dataDict:
		insts = dataDict[key]
		probs = []
		for d in range(len(insts[0])):
			sum = 1
			for i in range(len(insts)):
				instance = insts[i]
				sum += instance[d]
			probTrue = float(sum)/(len(insts)+2)
			probFalse = 1 - probTrue
			probs.append((probFalse, probTrue))
			#probs.append(sum)
			#print(dataDict[0][i])
			#print("_")
		probsForClass[key] = None
		
		probsForClass[key] = float(len(insts))/float(total), probs
		
	#print(total)
	#print(probsForClass)
	return probsForClass


def classify(probDict, inst):
	highestProb = 0
	bestClass = 0
	sum = 0
	for key in probDict:
		productofProbs = probDict[key][0]

		for d in range(len(inst)):
			productofProbs *= probDict[key][1][d][inst[d]]
		sum += productofProbs
		print(key, productofProbs)
		if productofProbs > highestProb:
			highestProb = productofProbs
			bestClass = key
	#print(bestClass, highestProb)
	#print(sum)
	return(bestClass)

parser = argparse.ArgumentParser()
parser.add_argument('TrainingFile')
parser.add_argument('TestFile')
args = parser.parse_args()
#print(args)
trainingFile = args.TrainingFile
testFile = args.TestFile

#trainingFile = "spamLabelled.dat"
#trainingFile = "spoof.dat"
#testFile = "spamUnlabelled.dat"
#testFile = "spooftest.dat"
trainData = getData(trainingFile)
dataDict = seperateByClass(trainData)
probDict = calcProbs(dataDict)
print("probabilitys:")
print(probDict)
unLabeledData = getData(testFile)
#print(unLabeledData)
#
#print(unLabeledData[3])
if (len(unLabeledData[0]) != len(trainData[0]) - 1):
	raise Exception("test data does not have 1 less collom then train data")
print("")
print("predictions")
for inst in unLabeledData:
	print(inst)
	print("Class prediction:",classify(probDict, inst))
	print("--")

#classify(probDict, unLabeledData[3])

# for key in probDict:
# 	print(key)
# 	for featProb in probDict[key][1]:
# 		print(featProb[1]) 



