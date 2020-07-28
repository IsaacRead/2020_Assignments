
from deap import base, creator
import random
from deap import tools
from deap import base
from deap import creator
from deap import gp
from deap import tools
from deap import algorithms
import numpy
import operator
import math

def GetData(file):
	fileText = file.readlines()
	x = []
	y = [] 
#	print(len(file))
	for line in fileText[1:]:
#		print(line)
		instance = line.split()
		if len(instance) !=37:
			raise Exception("something wrong with file")
		#print ( instance[:-1])
		x.append( list(map(float, instance[:-1])))		
		y.append(instance[-1])	

	return x,y

def percentOf(left, right):
    return ((left/100) * right)

pset = gp.PrimitiveSet("MAIN", 36)
pset.addPrimitive(operator.add, 2)
#pset.addPrimitive(operator.sub, 2)
pset.addPrimitive(operator.mul, 2)
#pset.addPrimitive(protectedDiv, 2)
pset.addPrimitive(percentOf, 2)
pset.addPrimitive(operator.neg, 1)
#pset.addPrimitive(math.cos, 1)
#pset.addPrimitive(math.sin, 1)
pset.addPrimitive(abs, 1)
#pset.addPrimitive(math.exp, 1)
#pset.addEphemeralConstant("rand101", lambda: random.randint(-1,1))
#pset.addEphemeralConstant("randint", lambda: random.randint(0,2))
pset.addEphemeralConstant("randfloat", lambda: random.random())


pset.renameArguments(ARG0='x')
#maximise so weights are positive
creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMin)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genHalfAndHalf, pset=pset, min_=1, max_=2)
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)
toolbox.register("compile", gp.compile, pset=pset)

def evalSymbReg(individual, xPoints, targets):
    # Transform the tree expression in a callable function
    func = toolbox.compile(expr=individual)
    errors = []
    for i in range(len(xPoints)):
        retValue = func(*xPoints[i])
        target = targets[i]
        if retValue < 0 and target == "'Normal'":
            errors.append(1 )
        elif retValue >= 0 and target == "'Anomaly'":
            errors.append(1 )
        elif target != "'Normal'" and target != "'Anomaly'":
            raise Exception("somethings wrong with data", targets[i])
    return math.fsum(errors)/ len(xPoints),

    # sqerrors = []
    # for i in range(len(xPoints)):
    #     retValue = func(*xPoints[i])
    #     target = 0
    #     error = 0
    #     if targets[i] == "'Anomaly'":
    #         target = 1
    #         error = (retValue - target)
    #     elif targets[i] == "'Normal'":
    #         target = -1
    #         error = (retValue - target)
    #     else:
    #         raise Exception("target was not correct", targets[i])


    #     sqerrors.append(error)


 

    # errors = []
    # for i in range(len(xPoints)):
    #     #should return >= 0 if Anomaly, < 0 if normal
    #     retValue = func(*xPoints[i])
    #     if (retValue == 0):
    #         sqerrors.append(1)
    #     else:
    #         if targets[i] == "'Anomaly'":
    #             if(retValue < 0):
    #                 sqerrors.append(1)
    #         elif targets[i] == "'Normal'":
    #              if(retValue > 0):
    #                 sqerrors.append(1)
    #         else:
    #             raise Exception("target was not correct", targets[i])

    # return math.fsum(errors) / len(xPoints),

    #  return math.fsum(sqerrors) / len(xPoints),
    # scores = []
    # for i in range(len(xPoints)):
    #     #should return >= 0 if Anomaly, < 0 if normal
    #     retValue = func(*xPoints[i])
    #     if targets[i] == "'Anomaly'":
    #             scores.append(retValue)
    #     elif targets[i] == "'Normal'":
    #             scores.append(-retValue)
    #     else:
    #         raise Exception("target was not correct", targets[i])


    # return math.fsum(scores) / len(xPoints),

trainFile = open("part4Data/training.txt", "r")
xPoints_train, targets_train = GetData(trainFile)
# print(xPoints)
# print(targets)
toolbox.register("evaluate", evalSymbReg, xPoints=xPoints_train, targets=targets_train)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("mate", gp.cxOnePoint)
toolbox.register("expr_mut", gp.genFull, min_=0, max_=2)
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)

toolbox.decorate("mate", gp.staticLimit(key=operator.attrgetter("height"), max_value=17))
toolbox.decorate("mutate", gp.staticLimit(key=operator.attrgetter("height"), max_value=17))


stats_fit = tools.Statistics(lambda ind: ind.fitness.values)
stats_size = tools.Statistics(len)
mstats = tools.MultiStatistics(fitness=stats_fit, size=stats_size)
mstats.register("avg", numpy.mean)
mstats.register("std", numpy.std)
mstats.register("min", numpy.min)
mstats.register("max", numpy.max)

def measureAccuracy(xPoints, targets):
    global bestFunc
    correctCount = 0
    for i in range(len(xPoints)):
        ret = bestFunc(*xPoints[i])
        if ret >= 0 :
            #print( "Anomaly", ret)s
            if targets[i] == "'Normal'":
                correctCount +=1
                #print("correct")
        else:
            #print("Normal", ret)
            if targets[i] == "'Anomaly'":
                correctCount +=1
                #print("correct")
    return correctCount/len(xPoints)

testFile = open("part4Data/test.txt", "r")
xPoints_test, targets_test = GetData(testFile)

testAccs = []
trainAccs = []
besttestacc = 0
overallBestFunc = None
bestFitness = 0
for j in range(10):
    pop = toolbox.population(n=500)
    hof = tools.HallOfFame(1)
    pop, log = algorithms.eaSimple(pop, toolbox, 0.5, 0.1, 60, stats=mstats, halloffame=hof, verbose=True)
    print(hof[0])
    bestFunc = toolbox.compile(expr=hof[0])
    testacc = measureAccuracy(xPoints_test, targets_test)
    if testacc > besttestacc:
        besttestacc = testacc
        overallBestFunc = hof[0]
        bestFitness = evalSymbReg(hof[0], xPoints_train, targets_train)
    print(testacc)
    testAccs.append(testacc)

    trainacc = measureAccuracy(xPoints_train, targets_train)
    trainAccs.append(trainacc)

print(len(testAccs))
print(len(trainAccs))

avgTestAcc = math.fsum(testAccs)/len(testAccs)
avgTrainAcc = math.fsum(trainAccs)/len(trainAccs)

print("besttestacc", besttestacc)
print("bestProg", overallBestFunc)
print("fitness:", bestFitness)

print("testAccs", testAccs)
print("---")
print("trainAccs", trainAccs)
print("avgTestAcc", avgTestAcc)
print("avgTrainAcc", avgTrainAcc)







