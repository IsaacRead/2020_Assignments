import random
file = open("part4Data/satellite", "r")

lines = file.readlines()
anomalies = []
normals = []
for line in lines[1:]:
	instance = line.split()
	if instance[-1] == "'Normal'":
		normals.append(line)
	elif instance[-1] == "'Anomaly'":
		anomalies.append(line)
	else:
		raise Exception("Somthing wrong with line", line)
print(len(normals))
print(len(anomalies))
random.shuffle(normals)
random.shuffle(anomalies)

numTestNormals = int(len(normals)*0.2)
#numTrainNormals = len(normals) - numTestNormals

numTestAnomalies = int(len(anomalies)*0.2)
#numTrainAnomalies = int(len(anomalies) - numTrainAnomalies)
testSet = []
trainSet = []
testSet.extend(normals[:numTestNormals])
testSet.extend(anomalies[:numTestAnomalies])

trainSet.extend(normals[numTestNormals:])
trainSet.extend(anomalies[numTestAnomalies:])

print(len(trainSet))
print(len(testSet))
print(len(lines) - 1)

print(testSet)
print("-----")
print(trainSet)

random.shuffle(testSet)
random.shuffle(trainSet)

testFile = open("part4Data/test.txt", "w")
trainFile = open("part4Data/training.txt", "w")

testFile.writelines(testSet)
trainFile.writelines(trainSet)





