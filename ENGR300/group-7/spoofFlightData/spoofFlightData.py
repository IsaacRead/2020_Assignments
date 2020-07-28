import random 
import time

START_LAT_LONG = (-41.28960147, 174.76899505)
END_LAT_LONG = (-41.2852965,174.77099597)
START_ALT = 100 #altidude of flight launch site in meters
#END_ALT = 100 #altidude of flight land site in meters
MAX_ALT = 500 #Maximum altidude of flight
FLIGHTIME = 20 #seconds of flight
STILLTIME = 5 #seconds of still after landing
INTERVAL = 0.5 #amount of seconds per update
#maximum magnitude of random variation per second for each lat and long values
#0.000001 corrasponds with 
MAX_VARI_PER_SECOND_LAT_LONG = 0.000001 
MAX_VARI_PER_SECOND_ALT = 1 

REALTIME = False

masterFile = open("FullFlightData.csv", "w+")
vari_lat_long = (MAX_VARI_PER_SECOND_LAT_LONG*INTERVAL)/2
vari_alt = (MAX_VARI_PER_SECOND_ALT*INTERVAL)/2

latEnd, lngEnd = END_LAT_LONG
lat_speed = (latEnd - START_LAT_LONG[0]) / (FLIGHTIME)
lng_speed = (lngEnd - START_LAT_LONG[1])/(FLIGHTIME)


def calcAltitude(second):
	global FLIGHTIME
	global MAX_ALT
	return (((-4*(MAX_ALT-START_ALT)*second)/(FLIGHTIME**2))*(second-FLIGHTIME) + START_ALT)

def calcLatLong(second):
	lat = round(second * lat_speed + START_LAT_LONG[0], 8)
	lng = round(second * lng_speed + START_LAT_LONG[1],8)

	return lat,lng

masterFile.write("seconds, latitude, longitude, altitude\n")
for t in range(int(FLIGHTIME / INTERVAL) + 1):
	second = float(t)*INTERVAL
	alt = calcAltitude(second )
	lat,lng = calcLatLong(second )

	if t>0 and t<FLIGHTIME/INTERVAL:
		alt += round(random.uniform(-vari_alt, vari_alt), 3)
		lat += round(random.uniform(-vari_lat_long, vari_lat_long), 7)
		lng += round(random.uniform(-vari_lat_long, vari_lat_long), 7)
	
	masterFile.write("{},{},{},{}\n".format(second,lat,lng,alt))

	intervalFile = open("flightData_{}.csv".format(t), "w+")
	#intervalFile.write("seconds, latitude, longitude, altitude\n")
	intervalFile.write("{},{},{},{}\n".format(second,lat,lng,alt))
	intervalFile.close()

	print(second,lat,lng,alt)
	if (REALTIME):
		time.sleep(INTERVAL)
	

	


	

