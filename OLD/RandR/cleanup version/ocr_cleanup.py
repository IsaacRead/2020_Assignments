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

def resize_img(img_path):
    im = Image.open(img_path)
    length_x, width_y = im.size
    factor = min(1, float(1024.0 / length_x))
    size = int(factor * length_x), int(factor * width_y)
    im_resized = im.resize(size, Image.ANTIALIAS)    
    return im_resized


def get_highlight_mask(img):
	#highlight colour range
	
	# rgb to HLS color spave conversion
	hls_img = cv2.cvtColor(img, cv2.COLOR_BGR2HLS)
		
	mask = cv2.inRange(hls_img, np.array([0, 0, 100]), np.array([255,255,255]))
		
	return mask
	

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
	img_resize = np.array(resize_img(img_path))	
	
	# Convert to gray
	img_filt = cv2.cvtColor(img_resize, cv2.COLOR_BGR2GRAY)			
	
	#apply addapdive thresholding
	img_filt = cv2.adaptiveThreshold(img_filt, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 17)
	mask_save_path = os.path.join(output_path, file_name + "_255_11_17_mask"  + ".jpg")
	#get highlighting mask
	HL_mask = get_highlight_mask(img_resize)	
	cv2.imwrite(mask_save_path, HL_mask)
	txt_save_path = os.path.join(output_path, file_name + "_255_11_17"  + ".txt")
	json_save_path = os.path.join(output_path, file_name + "json_255_11_17"  + ".json")	
	img_save_path = os.path.join(output_path, file_name + "_255_11_17"  + ".jpg")
	mask_save_path = os.path.join(output_path, file_name + "_255_11_17_mask"  + ".jpg")
	filt_save_path = os.path.join(output_path, file_name + "_255_11_17_filt"  + ".jpg")	
	cv2.imwrite(filt_save_path, img_filt)
	#get text from page
	result = pytesseract.image_to_string(img_filt, lang="eng", output_type=Output.STRING)
	
	#save the highlight mask
	cv2.imwrite(mask_save_path, HL_mask)	
	
	# write output to txt file
	f = open(txt_save_path,"w+")
	f.write(result)
	f.close() 	

	#get box and text data
	d = pytesseract.image_to_data(img_filt, output_type=Output.DICT)	
	
	#create and populate nested array for json output
	counter = 0
	jsondata = ''
	n_blocks = max(d['block_num'])
	arrOfBlocks = []
	for currblk in range(n_blocks):
		arrOfWords = []
		#iterate through each word in current block		
		while(d['block_num'][counter] == currblk):
			text = d['text'][counter]
			#if text is empty then skip
			if(text == '' or text == ' '): 
				counter += 1
				continue
			#get box coordinates for current word
			x = int(d['left'][counter])
			y = int(d['top'][counter])
			w = int(d['width'][counter])
			h = int(d['height'][counter])			
			#set region of interest to just the current box + 1 pixel buffer on each side
			roi = HL_mask[y-1:y + 1 + h, x-1:x+1+w]
			#count number of white pixels in word box on highight mask
			whitecount = cv2.countNonZero(roi)			
			totalpix = h * w
			whiteratio = whitecount / totalpix
			#check if word is more than %50 highlighted
			if(whiteratio > 0.3):
				HL = True
			else:
				HL = False
			#create word tuple with text info and highlight boolean
			word = (text, HL)
			#add word tupple to array	
			arrOfWords.append(word)
			counter += 1
		#if the array of words is not empty then add to the block array
		if (len(arrOfWords) != 0):
			arrOfBlocks.append(arrOfWords)

	#generate json text from nested array
	jsondata = json.dumps(arrOfBlocks, sort_keys=True, indent=4, separators=(',', ': '))
	#save the json file
	f = open(json_save_path,"w+")
	f.write(jsondata)
	f.close() 	
	return 

#set argument parser to get image file
ap = argparse.ArgumentParser()
ap.add_argument('-i', action='store', dest='image_path', help='Store a simple value')
args = ap.parse_args()
imagePath = args.image_path
get_string(imagePath)
