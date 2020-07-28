#https://medium.com/cashify-engineering/improve-accuracy-of-ocr-using-image-preprocessing-8df29ec3a033

import numpy as np
import pytesseract
import argparse
import cv2
import os
import tempfile
from PIL import Image
from pytesseract import Output

def set_image_dpi(file_path):
    im = Image.open(file_path)
    length_x, width_y = im.size
    factor = min(1, float(1024.0 / length_x))
    size = int(factor * length_x), int(factor * width_y)
    im_resized = im.resize(size, Image.ANTIALIAS)
    temp_file = tempfile.NamedTemporaryFile(delete=False,   suffix='.png')
    temp_filename = temp_file.name
    im_resized.save(temp_filename, dpi=(200, 200))
    return temp_filename

def image_smoothening(img):
    ret1, th1 = cv2.threshold(img, 200, 255, cv2.THRESH_BINARY)
    ret2, th2 = cv2.threshold(th1, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    blur = cv2.GaussianBlur(th2, (1, 1), 0)
    ret3, th3 = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    return th3

def remove_noise_and_smooth(file_name):
    img = cv2.imread(file_name, 0)
    filtered = cv2.adaptiveThreshold(img.astype(np.uint8), 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 9, 41)
    kernel = np.ones((1, 1), np.uint8)
    opening = cv2.morphologyEx(filtered, cv2.MORPH_OPEN, kernel)
    closing = cv2.morphologyEx(opening, cv2.MORPH_CLOSE, kernel)
    #img = image_smoothening(img)
    or_image = cv2.bitwise_or(img, closing)
    return or_image


def get_string(img_path):
	# Read image using opencv
	#img = cv2.imread(img_path)
	# Extract the file name without the file extension
	file_name = os.path.basename(img_path).split('.')[0]
	file_name = file_name.split()[0]
    # Create a directory for outputs
	cwd = os.getcwd()

	output_path = os.path.join(cwd, file_name)
	if not os.path.exists(output_path):	
		os.makedirs(output_path)
		
	# Rescale the image, if needed.
	img_resize = np.array(Image.open(set_image_dpi(img_path)))
	
	#img_resize = remove_noise_and_smooth(img_resize)
	   # Convert to gray
	img_resize = cv2.cvtColor(img_resize, cv2.COLOR_BGR2GRAY)
	# Apply dilation and erosion to remove some noise
	kernel = np.ones((1, 1), np.uint8)
	img_resize = cv2.dilate(img_resize , kernel, iterations=1)
	img_resize = cv2.erode(img_resize, kernel, iterations=1)
	# Apply blur to smooth out the edges
	#img_resize = cv2.GaussianBlur(img_resize, (5, 5), 0)	
	#img_resize = cv2.threshold(img_resize, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]
	#cv2.adaptiveThreshold(img_resize, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 2)
	img_resize = cv2.adaptiveThreshold(img_resize, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 115, 1)
	# Save the filtered image in the output directory
	
	img_save_path = os.path.join(output_path, file_name + "_filter_"  + ".jpg")
	cv2.imwrite(img_save_path, img_resize)

    # Recognize text with tesseract for python
	txt_save_path = os.path.join(output_path, file_name + "_out_resiz"  + ".txt")		
	result = pytesseract.image_to_string(img_resize, lang="eng")
	# write output to txt file
	f = open(txt_save_path,"w+")
	f.write(result)
	f.close() 
	
	

	d = pytesseract.image_to_data(img_resize, output_type=Output.DICT)
	n_boxes = len(d['level'])
	for i in range(n_boxes):
	#(x, y, w, h) = (d['left'][i], d['top'][i], d['width'][i], d['height'][i])
		x = int(d['left'][i])
		y = int(d['top'][i])
		w = int(d['width'][i])
		h = int(d['height'][i])
		
		#cv2.rectangle(img_resize, (x, y), (x + w, y + h), (0, 255, 0), 2)
		#cv2.rectangle(img_resize, (x, y), (x + w, y + h), (255, 255, 0), 2)
		cv2.rectangle(img_resize, (x, y), (x+w, y+h), 2)
		print (x)
		print (y)
		print (w)
		print (h)
		print (' ')
	cv2.imshow('img', img_resize)
	cv2.waitKey(0)
	return 



ap = argparse.ArgumentParser()
ap.add_argument('-i', action='store', dest='image_path', help='Store a simple value')
args = ap.parse_args()
imagePath = args.image_path
get_string(imagePath)
