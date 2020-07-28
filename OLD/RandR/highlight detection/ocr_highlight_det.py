#https://medium.com/cashify-engineering/improve-accuracy-of-ocr-using-image-preprocessing-8df29ec3a033

import numpy as np
import pytesseract
import json
import argparse
import cv2
import os
import tempfile
from PIL import Image
from pytesseract import Output

def set_image_dpi(file_name):
	im = Image.open(file_name)
	length_x, width_y = im.size
	#length_x, width_y  = im.shape
	factor = min(1, float(1024.0 / length_x))
	size = int(factor * length_x), int(factor * width_y)
	im_resized = im.resize(size, Image.ANTIALIAS)
	#im_resized = cv2.resize(im, None, fx = factor, fy = factor, interpolation = cv2.INTER_AREA)
	return im_resized


def get_highlight_mask(img):
	#highlight colour range
	hsv_lower=[0, 80, 120] 
	hsv_upper=[180, 255, 255]
	
	# rgb to HSV color spave conversion
	hsv_img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
	HSV_lower = np.array(hsv_lower, np.uint8)  # Lower HSV value
	HSV_upper = np.array(hsv_upper, np.uint8)  # Upper HSV value
	#Threshold
	HL_mask = cv2.inRange(hsv_img, HSV_lower, HSV_upper)

	return HL_mask
	# find connected components
	#_, contours, hierarchy, = cv2.findContours(frame_threshed, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

def get_string(img_path):
	# Extract the file name without the file extension
	file_name = os.path.basename(img_path).split('.')[0]
	file_name = file_name.split()[0]
    # Create a directory for outputs
	cwd = os.getcwd()

	output_path = os.path.join(cwd, file_name)
	if not os.path.exists(output_path):	
		os.makedirs(output_path)
		
	# Rescale the image, if needed.
	#og_img = cv2.imread(file_name)
	img_resize = np.array(set_image_dpi(img_path))
	
	#get highlighting mask
	HL_mask = get_highlight_mask(img_resize)
	
	# Convert to gray
	img_filt = cv2.cvtColor(img_resize, cv2.COLOR_BGR2GRAY)		
	
	#apply addapdive thresholding
	img_filt = cv2.adaptiveThreshold(img_filt, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 17)
    
	txt_save_path = os.path.join(output_path, file_name + "_255_11_17"  + ".txt")
	json_save_path = os.path.join(output_path, file_name + "json_255_11_17"  + ".json")	
	img_save_path = os.path.join(output_path, file_name + "_255_11_17"  + ".jpg")
	mask_save_path = os.path.join(output_path, file_name + "_255_11_17_mask"  + ".jpg")	
	
	result = pytesseract.image_to_string(img_filt, lang="eng", output_type=Output.STRING)	
	
	
	# write output to txt file
	f = open(txt_save_path,"w+")
	f.write(result)
	f.close() 	

	d = pytesseract.image_to_data(img_filt, output_type=Output.DICT)	
	
	counter = 0
	jsondata = ''
	n_blocks = max(d['block_num'])
	arrOfBlocks = []
	for currblk in range(n_blocks):
		arrOfWords = []		
		while(d['block_num'][counter] == currblk):
			text = d['text'][counter]
			if(text == '' or text == ' '): 
				counter += 1
				continue
			x = int(d['left'][counter])
			y = int(d['top'][counter])
			w = int(d['width'][counter])
			h = int(d['height'][counter])
				
			cv2.rectangle(img_filt, (x, y), (x+w, y+h), 2)
			cv2.rectangle(HL_mask, (x, y), (x+w, y+h), (100,200,100), 2)
			roi = HL_mask[y:y + h, x:x+w]
			whitecount = cv2.countNonZero(roi)
			totalpix = h * w
			whiteratio = whitecount / totalpix
			if(whiteratio > 0.3):
				#print (d['text'][i])
				HL = True
			else:
				HL = False
			word = (text, HL)	
			arrOfWords.append(word)
			counter += 1
		if (len(arrOfWords) != 0):
			arrOfBlocks.append(arrOfWords)
	jsondata = json.dumps(arrOfBlocks, sort_keys=True, indent=4, separators=(',', ': '))
	
	f = open(json_save_path,"w+")
	f.write(jsondata)
	f.close() 
	cv2.imwrite(mask_save_path, HL_mask)
	cv2.imshow('img', img_filt)
	cv2.imwrite(img_save_path, img_filt)
	cv2.waitKey(0)
	return 



ap = argparse.ArgumentParser()
ap.add_argument('-i', action='store', dest='image_path', help='Store a simple value')
args = ap.parse_args()
imagePath = args.image_path
get_string(imagePath)
