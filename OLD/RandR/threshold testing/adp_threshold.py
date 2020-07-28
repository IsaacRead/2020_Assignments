import cv2 as cv
import numpy as np
from matplotlib import pyplot as plt
img_path = compxpg1.jpg
file_name = os.path.basename(img_path).split('.')[0]
file_name = file_name.split()[0]
# Create a directory for outputs
cwd = os.getcwd()

output_path = os.path.join(cwd, file_name)
if not os.path.exists(output_path):	
	os.makedirs(output_path)
img = cv.imread(img_path ,0)
length_x, width_y = im.size
factor = min(1, float(1024.0 / length_x))
size = int(factor * length_x), int(factor * width_y)
im_resized = im.resize(size, Image.ANTIALIAS)
img = cv.medianBlur(img,5)
ret,th1 = cv.threshold(img,127,255,cv.THRESH_BINARY)
th2 = cv.adaptiveThreshold(img,255,cv.ADAPTIVE_THRESH_MEAN_C,\
            cv.THRESH_BINARY,11,2)
th3 = cv.adaptiveThreshold(img,255,cv.ADAPTIVE_THRESH_GAUSSIAN_C,\
            cv.THRESH_BINARY,11,2)
titles = ['Original Image', 'Global Thresholding (v = 127)',
            'Adaptive Mean Thresholding', 'Adaptive Gaussian Thresholding']
images = [img, th1, th2, th3]
for i in range(4):
    #plt.subplot(2,2,i+1),plt.imshow(images[i],'gray')
    #plt.title(titles[i])
    #plt.xticks([]),plt.yticks([])
	txt_save_path = os.path.join(output_path, file_name + "_out_" + i  + ".txt")		
	result = pytesseract.image_to_string(img_resize, lang="eng")
plt.show()