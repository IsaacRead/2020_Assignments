import argparse

#parser = argparse.ArgumentParser()
#parser.add_argument('TrainingFile')
#parser.add_argument('TestFile')
#args = parser.parse_args()
#print(args)
#trainFilePath = args.TrainingFile
#testFilePath = args.TestFile

trainSet = []
testSet = []
Accuracys = []
#print(trainTxt)
#print(testTxt)
INDSIZE = "   "

class InnerNode:
	def __init__(self, att, left, right):
		#print("creating innerNode")
		self.att = att
		#print("best att", self.att)
		self.left = left
		self.right = right
		
	def Report(self, indent):
		print(indent+ self.att+ "= True:")
		self.left.Report(indent+INDSIZE)
		print(indent+ self.att+ "= False:")
		self.right.Report(indent+INDSIZE)
		
class LeafNode:
	def __init__(self, classPred, prob):
		#print("creating leaf node")	
		self.classPred = classPred
		#print("classpred", self.classPred)
		self.prob = prob	
		#print("prob", self.prob)
	
	def Report(self, indent):
		print(indent + "Class: " + self.classPred + " prob=" + str(self.prob))
				
class Instance:
	def __init__(self, dataLine, attrNames):		
		self.attVals = {}		
		#print("dataLine", dataLine)		
		if not len(dataLine) == attNum:
			raise ValueError("line does not have right number of dimensions")		
		self.classVal = dataLine[0]
		for index, dim in enumerate(dataLine[1:]):			
			if dim.lower() == "true":				
				self.attVals[attrNames[index]] = True
			elif dim.lower() == "false":
				self.attVals[attrNames[index]]  = False	
		#print("attValues", self.attVals)
		#print("class", self.classVal)

def GetData(text):
	formatedData = []
	for line in text[1:]:		
		lineList = line.split()
		#print("line:",lineList)
		currInst = Instance(lineList, initAttributes)
		#print(currInst)
		formatedData.append(currInst)	
		#print("-----")
	return formatedData


def CountClass(instList):
	classCounter = {}	
	for inst in instList:
		if inst.classVal in classCounter:
			classCounter[inst.classVal] += 1
		else:
			classCounter[inst.classVal] = 1
	aux = [(classCounter[key], key) for key in classCounter]
	aux.sort(reverse = True)
	return aux

def CalcMostProb(instList):
	classCount = CountClass(instList)
	#print("classCount",classCount)
	mostProbClass = classCount[0][1]
	#print("mostProbClass",mostProbClass)
	prob = classCount[0][0] / float(len(instList))	
	#print("prob",prob)
	return (mostProbClass, prob)
	
def CalcImpurity(instList):
	classCount = CountClass(instList)
	#print("classCount",classCount)
	if len(classCount) <= 1: #if only 1 or no class present return 0 impurity
	#	print("pure!-------")
		return 0
	
	if not (classCount[0][0] + classCount[1][0]) == len(instList):
		raise Exception("class count and inst do not match")
	if len(classCount) > 2:
		raise Exception("has not been programmed for more then 2 class values")
	imp = (classCount[0][0] * classCount[1][0]) / float(len(instList)**2)
	#print("imp",imp)
	return imp
	
	

def BuildTree(indent, nodeInsts, nodeAttrs):
	#print("")
	#print(indent+"newbuildTree----")
	#print(indent+"numInsts:", len(nodeInsts))
	#print(indent+"nodeAttrs", nodeAttrs)
	
	#if instances is empty
	if len(nodeInsts) == 0:
		#print(indent+"instances empty")
		globalProbClass, prob = CalcMostProb(trainSet) #calc most probable class across whole training sets		
		return LeafNode(globalProbClass, prob) #baseline leafnode, most prob class across whole dataset
		
	#print("calc local probable Class")
	localProbClass, prob = CalcMostProb(nodeInsts)
	#if instances are pure
	if prob == 1:
		#print(indent+"insts pure")
		return LeafNode(localProbClass, prob)
	#if attributes is empty
	if len(nodeAttrs) == 0:
		#print(indent+"attributes empty")			
		return LeafNode(localProbClass, prob)
	
	#else find best attribute	
	#print(indent+"finding best attribute\n")
	bestWtdImp = 1
	bestInstsTrue = []
	bestInstsFlase = []
	bestAtt = "_init_"
	for att in nodeAttrs:
		trueInsts = []
		falseInsts = []
		for inst in nodeInsts:
			if inst.attVals[att] == True:
				trueInsts.append(inst)
			else:
				falseInsts.append(inst)
		
		#print(indent+"att:", att)
		#print(indent+"true len", len(trueInsts))
		#print(indent+"false len", len(falseInsts))
		trueImp = CalcImpurity(trueInsts)
		#print(indent+"trueImp", trueImp)
		falseImp = CalcImpurity(falseInsts)
		#print(indent+"falseImp", falseImp)
		
		wtdAvgImp = trueImp * (len(trueInsts) / float(len(nodeInsts))) + falseImp * (len(falseInsts) / float(len(nodeInsts)))
		if wtdAvgImp < bestWtdImp:
			bestWtdImp = wtdAvgImp
			bestAttr = att
			bestInstsTrue = trueInsts
			bestInstsFalse = falseInsts
		#print(indent+"wtdAvgImp",wtdAvgImp)
		#print("")
	#print(indent+"bestAttr",bestAttr)
	#print(indent+"bestWtdImp",bestWtdImp)
	#print(indent+"numTrue:", len(bestInstsTrue))
	#print(indent+"numFalse:", len(bestInstsFalse))
	newAttrs = nodeAttrs[:]
	newAttrs.remove(bestAttr)
	#print(indent+ bestAttr + "----building left with", newAttrs)
	left = BuildTree(indent + INDSIZE, bestInstsTrue, newAttrs)
	#print(indent+bestAttr + "----building right with", newAttrs)
	right = BuildTree(indent + INDSIZE, bestInstsFalse, newAttrs)
	#print(indent+"returning node after this ---")
	return InnerNode(bestAttr, left, right)


def FindPrediction(testInst, currNode):
	#print(type(currNode))
	if isinstance(currNode, LeafNode):
		ret = (currNode.classPred, currNode.prob)
		return ret
	if testInst.attVals[currNode.att] == True:
		return FindPrediction(testInst, currNode.left)
	return FindPrediction(testInst, currNode.right)
	

trainBase = "hepatitis-training-run-"
testBase = "hepatitis-test-run-"
for x in range(10):
	trainFilePath = trainBase + str(x)
	testFilePath = testBase + str(x)
	print("Training File:",trainFilePath)
	print("Test File: ",testFilePath)

	trainFile = open(trainFilePath, "r")
	testFile = open(testFilePath,"r")
	trainTxt = trainFile.readlines()
	testTxt = testFile.readlines()			
	#classes = trainTxt[0].split()
	initAttributes = trainTxt[0].split()
	initAttributes = initAttributes[1:]
	#print("classes", classes)
	print("attributes:", initAttributes)
	print("")

	attNum = len(initAttributes) + 1#num dimensions plus 1 class value

	trainSet = GetData(trainTxt)
	
	testSet = GetData(testTxt)
	
	rootNode = BuildTree("", trainSet, initAttributes)
	rootNode.Report("")


	correctCount = 0
	falseCount = 0
	for inst in testSet:
		classPred, prob = FindPrediction(inst, rootNode)
		#print("___")
		#print("instance: ", inst.attVals)
		#print("prediction: " + classPred +  " " + str(prob))
		#print("actual: " + inst.classVal)
		if classPred == inst.classVal:
			  print("correct!")
			  correctCount +=1
		else:
			  print("Incorrect")
			  falseCount += 1
	print("")
	print("Correct Predictions: ", correctCount)
	print("Incorrect Predictions: ", falseCount)
	print("Acuracy: ", (correctCount/ float(falseCount+correctCount)))
	Accuracys.append( (correctCount/ float(falseCount+correctCount)))
accSum = 0
count = 0
for acc in Accuracys:
	print(count, acc)
	accSum += acc
	count+=1
print("Acc Avg:", accSum / float(10))





