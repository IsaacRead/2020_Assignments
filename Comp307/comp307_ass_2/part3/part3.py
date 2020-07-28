
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
	for line in fileText[1:]:
		instance = list(map(float,line.split()))
		if len(instance) !=2:
			raise Exception("something wrong with file")
		
		x.append(instance[0])		
		y.append(instance[1])	

	return x,y

def percentOf(left, right):
	return ((left/100) * right)

pset = gp.PrimitiveSet("MAIN", 1)
pset.addPrimitive(operator.add, 2)
pset.addPrimitive(operator.mul, 2)
pset.addPrimitive(percentOf, 2)
pset.addPrimitive(operator.neg, 1)
pset.addPrimitive(math.cos, 1)
pset.addPrimitive(math.sin, 1)
pset.addEphemeralConstant("randfloat", lambda: random.random())
#pset.addEphemeralConstant("randint", lambda: random.randint(0,2))

pset.renameArguments(ARG0='x')

creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMin)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genHalfAndHalf, pset=pset, min_=1, max_=2)
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)
toolbox.register("compile", gp.compile, pset=pset)

def evalSymbReg(individual, xPoints, yPoints):
	print("type(individual)")
	print(type(individual))
	func = toolbox.compile(expr=individual)
	print("type(func)")
	print(type(func))
	sqerrors = []
	for i in range(len(xPoints)):
		sqerrors.append((func(xPoints[i]) - yPoints[i])**2)
	return math.fsum(sqerrors) / len(xPoints),

trainFile = open("part3Data/regression", "r")
xPoints, yPoints = GetData(trainFile)
toolbox.register("evaluate", evalSymbReg, xPoints=xPoints, yPoints=yPoints)
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

mses = 0
Mses = []
minMse = 100
best = None
for i in range(10):
	pop = toolbox.population(n=500)
	hof = tools.HallOfFame(1)
	pop, log = algorithms.eaSimple(pop, toolbox, 0.5, 0.1, 60, stats=mstats, halloffame=hof, verbose=True)
	print(hof[0])
	mse, = evalSymbReg(hof[0], xPoints, yPoints)
	if mse < minMse:
		minMse = mse
		best = hof[0]
	print(mse)
	mses += mse
	Mses.append(mse)
print(Mses)
print("avg",mses/10)
print("min", minMse)
print("best ", best)
