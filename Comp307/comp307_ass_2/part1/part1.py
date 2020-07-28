class DataPoint:
	def __init__(self, f, t):
		self.featureVals = f
		self.target = t


def getDataFromFile(fileName):		
	data = []
	with open(fileName,'r') as dataFile:
		lines = dataFile.readlines()[1:]
		dimNum = len(lines[0].split()) - 1
		
		for line in lines:
			values = list(map(int, line.split()))	
			fVals = values[:-1]
			fVals.insert(0, 1)
			print(fVals)
			d = DataPoint(fVals, values[-1])
			data.append(d)
		return data, dimNum
	
def classify(featureVals, weights):
	global dimNum
	dotProd = 0
	for d in range(0, dimNum + 1):
		dotProd += weights[d] * featureVals[d]
	prediction = 0
	if dotProd > 0:
		prediction = 1
	return prediction

def updateWeights(oldWs, direction, featureVals):
	global LR
	newWs = []
	for d in range(dimNum+1):
		newWs.append(oldWs[d] + LR*direction*featureVals[d])
	
	return newWs

def testAccuracy(weights):
	global data
	correctCount = 0
	for dPoint in data:
		pred = classify(dPoint.featureVals, weights)
		if(pred == dPoint.target):
			correctCount +=1 
			#print(correctCount)
			
	return correctCount / float(len(data))

import random

data, dimNum = getDataFromFile("part1Data/dataset")
print(dimNum)
weights = []
for d in range(dimNum + 1):
	weights.append(0.5)
print(weights)
epoch = 0
accuracy = 0 
LR = 0.1
while(epoch <= 200 and accuracy < 1):
	print("------")
	dPoint = data[epoch % len(data)]
	#print("data point", dPoint.featureVals, dPoint.target)
	prediction = classify(dPoint.featureVals, weights)
	#print("prediction", prediction)
	if (prediction > dPoint.target):
		print("decrease weights")
		weights = updateWeights(weights, -1, dPoint.featureVals)
	elif(prediction < dPoint.target):
		print("increase weights")
		weights = updateWeights(weights, 1, dPoint.featureVals)
	else:
		print("correct")
	#print("weight", weights)
	accuracy = testAccuracy(weights)
	print("Epoch: ",epoch)
	print("Accuracy: ",accuracy)
	epoch += 1

