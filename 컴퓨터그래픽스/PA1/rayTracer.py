#!/usr/bin/env python3
# -*- coding: utf-8 -*
# sample_python aims to allow seamless integration with lua.
# see examples below

import os
import sys
import pdb  # use pdb.set_trace() for debugging
import code # or use code.interact(local=dict(globals(), **locals()))  for debugging.
import xml.etree.ElementTree as ET
import numpy as np
import math
from PIL import Image
class Color:
    def __init__(self, R, G, B):
        self.color=np.array([R,G,B]).astype(np.float)

    # Gamma corrects this color.
    # @param gamma the gamma value to use (2.2 is generally used).
    def gammaCorrect(self, gamma):
        inverseGamma = 1.0 / gamma;
        self.color=np.power(self.color, inverseGamma)

    def toUINT8(self):
        return (np.clip(self.color, 0,1)*255).astype(np.uint8)


def setDefault(className):
    className.shader = {}
    className.surface = []
    className.light = []
    className.projDistance = -1
    for c in className.root.findall('camera'):
        className.viewPoint = np.array(c.findtext('viewPoint').split()).astype(np.float)
        className.viewDir = np.array(c.findtext('viewDir').split()).astype(np.float)
        if(c.findtext('projDistance')):
            className.projDistance = np.array(c.findtext('projDistance').split()).astype(np.float)
        className.projNormal = np.array(c.findtext('projNormal').split()).astype(np.float)
        className.viewUp = np.array(c.findtext('viewUp').split()).astype(np.float)
        className.viewWidth = np.array(c.findtext('viewWidth').split()).astype(np.float)
        className.viewHeight = np.array(c.findtext('viewHeight').split()).astype(np.float)

    for c in className.root.findall('shader'):
        name = c.get('name')
        dt = {}
        sType = c.get('type')
        dt['sType'] = sType
        dt['diffuseColor'] =  np.array(c.findtext('diffuseColor').split()).astype(np.float)
        if(sType == "Phong"):
            dt['specularColor'] = np.array(c.findtext('specularColor').split()).astype(np.float)
            dt['exponent'] = np.array(c.findtext('exponent').split()).astype(np.float)
        className.shader[name] = dt

    for c in className.root.findall('surface'):
        dt = {}
        typeName = c.get('type')
        dt['type'] = typeName
        ref = c.find('shader').attrib
        dt['ref'] = ref['ref']
        if(typeName == "Box"):
            dt['minPt'] = np.array(c.findtext('minPt').split()).astype(np.float)
            dt['maxPt'] = np.array(c.findtext('maxPt').split()).astype(np.float)
        elif(typeName == "Sphere"):
            dt['center'] = np.array(c.findtext('center').split()).astype(np.float)
            dt['radius'] = np.array(c.findtext('radius').split()).astype(np.float)
        className.surface.append(dt)

    for c in className.root.findall('light'):
        dt = {}
        dt['position'] = np.array(c.findtext('position').split()).astype(np.float)
        dt['intensity'] = np.array(c.findtext('intensity').split()).astype(np.float)
        className.light.append(dt)

def intersection_single(surface, p, d):
    if(surface['type'] == "Sphere"):
        center = surface['center']
        d_dot_p = np.dot(d, p-center)
        radius = surface['radius']
        t_sqrt_in = d_dot_p*d_dot_p - np.dot(p-center, p-center) + radius*radius
        t_del = np.sqrt(t_sqrt_in)

        t0 = -d_dot_p - t_del
        t1 = -d_dot_p + t_del

        if(t_sqrt_in < 0):
            # No intersection
            return False, t0
        elif(t_sqrt_in > 0):
            # Two solution
            if(t0 < 0 and t1 < 0):
                return False, t0
            t = min(t0, t1)
            return True, t
        else:
            # one solution
            t = t0
            return True, t
    
    elif(surface['type'] == "Box"):
        t_near = -math.inf
        t_far = math.inf
        minPt = surface['minPt']
        maxPt = surface['maxPt']

        for count in range(3):
            if(d[count] == 0):
                if(p[count] < minPt[count] or p[count] > maxPt[count]):
                    return False, t_near
            else:
                t1 = (minPt[count] - p[count]) / d[count]
                t2 = (maxPt[count] - p[count]) / d[count]
                t_min = min(t1, t2)
                t_MAX = max(t1, t2)

                if(t_min > t_near):
                    t_near = t_min
                if(t_MAX < t_far):
                    t_far = t_MAX

                if(t_near > t_far):
                    return False, t_near
                if(t_far < 0):
                    return False, t_near

        return True, t_near

def intersection_multiple(className, p, d):
    t_near = math.inf
    color = ""
    hitSurface = -1
    check_shadow = False
    for i in range(len(className.surface)):
        check, t = intersection_single(className.surface[i], p, d)
        if(check):
            check_shadow = True
            if(t_near > t):
                t_near = t
                color = className.surface[i]['ref']
                hitSurface = i

    return hitSurface, color, t_near, check_shadow

def shading_single(className, normal, v, l, color):
    if(className.shader[color]['sType'] == "Phong"):
        #diffuse color
        L_d = className.shader[color]['diffuseColor']*className.light[0]['intensity']*(max(0, np.dot(normal, l)))

        #specular shading
        h = (v+l) / np.sqrt(np.dot(v+l, v+l))
        L_s = className.shader[color]['specularColor']*className.light[0]['intensity']*math.pow(max(0, np.dot(normal, h)), className.shader[color]['exponent'])
    
        return L_d + L_s

def shading_multiple(className, p, d, t, color, hitSurface):
    L_d = np.array([0, 0, 0])
    L_s = np.array([0, 0, 0])

    if(className.surface[hitSurface]['type'] == "Sphere"):
        if(len(className.light) == 1):
            l_tmp = className.light[0]['position'] - (p+t*d) # light direction
            light = l_tmp / np.sqrt(np.dot(l_tmp, l_tmp))

            # shadows
            EPS = 0.0001
            hit, c, t_shadow, check = intersection_multiple(className, (p+t*d)+ EPS*light, light)
            if(check == False):
                Q = (p + t*d) - className.surface[hitSurface]['center']
                normal = Q / np.sqrt(np.dot(Q, Q)) # surface normal
                
                #diffuse color
                L_d = className.shader[color]['diffuseColor']*className.light[0]['intensity']*(max(0, np.dot(normal, light)))
            
            return L_d
        # multiple light
        else:
            Q = (p + t*d) - className.surface[hitSurface]['center']
            normal = Q / np.sqrt(np.dot(Q, Q)) # surface normal
            
            for i in range(len(className.light)):
                l_tmp = className.light[i]['position'] - (p+t*d) # light direction
                light = l_tmp / np.sqrt(np.dot(l_tmp, l_tmp))
                if(className.shader[color]['sType'] == "Lambertian"):
                    # shadows
                    EPS = 0.0001
                    hit, c, t_shadow, check = intersection_multiple(className, (p+t*d)+ EPS*light, light)
                    if(check == False):
                        L_d = L_d + className.shader[color]['diffuseColor']*className.light[i]['intensity']*(max(0, np.dot(normal, light)))
                    else:
                        L_d = L_d + np.array([0, 0, 0])
                else:
                    # shadows
                    EPS = 0.0001
                    hit, c, t_shadow, check = intersection_multiple(className, (p+t*d)+ EPS*light, light)
                    if(check == False):
                        eye_tmp = className.viewPoint - (p+t*d)
                        v = eye_tmp / np.sqrt(np.dot(eye_tmp, eye_tmp)) # eye direction
                        L_d = L_d + className.shader[color]['diffuseColor']*className.light[i]['intensity']*(max(0, np.dot(normal, light)))
                        #specular shading
                        h = (v+light) / np.sqrt(np.dot(v+light, v+light))
                        L_s = L_s + className.shader[color]['specularColor']*className.light[0]['intensity']*math.pow(max(0, np.dot(normal, h)), className.shader[color]['exponent'])
                    
                    else:
                        L_d = L_d + np.array([0, 0, 0])
                        L_s = L_s + np.array([0, 0, 0])

            return L_d+L_s

    elif(className.surface[hitSurface]['type'] == "Box"):
        bias = 1.000001
        center = (className.surface[hitSurface]['maxPt']+className.surface[hitSurface]['minPt'])/2
        Q_hit = (p + t*d) - center
        d_hit = (className.surface[hitSurface]['maxPt']-className.surface[hitSurface]['minPt'])/2
        Q = np.array([int(Q_hit[0]/d_hit[0]*bias), int(Q_hit[1]/d_hit[1]*bias), int(Q_hit[2]/d_hit[2]*bias)]).astype(np.float)
        normal = Q / np.sqrt(np.dot(Q, Q)) # surface normal
        
        if(len(className.light) == 1):
            pass
        else:
            for i in range(len(className.light)):
                l_tmp = className.light[i]['position'] - (p+t*d) # light direction
                light = l_tmp / np.sqrt(np.dot(l_tmp, l_tmp))
                if(className.shader[color]['sType'] == "Lambertian"):
                    # shadows
                    EPS = 0.0001
                    hit, c, t_shadow, check = intersection_multiple(className, (p+t*d)+ EPS*light, light)
                    if(check == False):
                        L_d = L_d + className.shader[color]['diffuseColor']*className.light[i]['intensity']*(max(0, np.dot(normal, light)))
                    else:
                        L_d = L_d + np.array([0, 0, 0])
                else:
                    # shadows
                    EPS = 0.0001
                    hit, c, t_shadow, check = intersection_multiple(className, (p+t*d)+ EPS*light, light)
                    if(check == False):
                        L_d = L_d + className.shader[color]['diffuseColor']*className.light[i]['intensity']*(max(0, np.dot(normal, light)))
                        #specular shading
                        h = (v+light) / np.sqrt(np.dot(v+light, v+light))
                        L_s = L_s + className.shader[color]['specularColor']*className.light[0]['intensity']*math.pow(max(0, np.dot(normal, h)), className.shader[color]['exponent'])
                    
                    else:
                        L_d = L_d + np.array([0, 0, 0])
                        L_s = L_s + np.array([0, 0, 0])

            return L_d+L_s
        
def cameraCoordin(className):
    w = className.projNormal / np.sqrt(np.dot(className.projNormal, className.projNormal))
    U = np.cross(className.viewUp, w)
    u = U / np.sqrt(np.dot(U, U))
    v = np.cross(u, w)

    return np.array([u, v, w])

def drawImg(className, imgSize, img):
    coordinate = cameraCoordin(className)
    # y-axis
    for y in np.arange(imgSize[1]):
        v_coefficient = -className.viewHeight/2 + className.viewHeight*(y + 0.5)/imgSize[1]
        # x-axis
        for x in np.arange(imgSize[0]):
            # pixel to image mapping
            u_coeifficient = -className.viewWidth/2 + className.viewWidth*(x + 0.5)/imgSize[0]

            # compute viewing ray
            p = className.viewPoint
            if(className.projDistance != -1):
                s = className.viewPoint + u_coeifficient*coordinate[0] + v_coefficient*coordinate[1] - (className.projDistance)*coordinate[2]
            else:
                s = className.viewPoint + u_coeifficient*coordinate[0] + v_coefficient*coordinate[1] - coordinate[2]
            D = s - className.viewPoint
            d = D / np.sqrt(np.dot(D,D))

            #intersect ray with scene
            if(len(className.surface) == 1):
                check, t = intersection_single(className.surface[0], p, d)
                if(check):
                    if(className.surface[0]['type'] == "Sphere"):
                        # compute illumination at visible point
                        Q = (p + t*d) - className.surface[0]['center']
                        normal = Q / np.sqrt(np.dot(Q, Q)) # surface normal
                        eye_tmp = className.viewPoint - (p+t*d)
                        eye = eye_tmp / np.sqrt(np.dot(eye_tmp, eye_tmp)) # eye direction
                        l_tmp = className.light[0]['position'] - (p+t*d) # light direction
                        light = l_tmp / np.sqrt(np.dot(l_tmp, l_tmp))
                    elif(className.surface[0]['type'] == "Box"):
                        # compute illumination at visible point
                        Q_hit = p + t*d
                        d_hit = (className.surface[0]['maxPt']-className.surface[0]['minPt'])/2
                        Q = np.array([Q_hit[0]/d_hit[0], Q_hit[1]/d_hit[1], Q_hit[2]/d_hit[2]]).astype(int)
                        normal = Q / np.sqrt(np.dot(Q, Q)) # surface normal
                        eye_tmp = className.viewPoint - (p+t*d)
                        eye = eye_tmp / np.sqrt(np.dot(eye_tmp, eye_tmp)) # eye direction
                        l_tmp = className.light[0]['position'] - (p+t*d) # light direction
                        light = l_tmp / np.sqrt(np.dot(l_tmp, l_tmp))

                    L = shading_single(className, normal, eye, light, className.surface[0]['ref'])
                    className.color = Color(L[0], L[1], L[2])
                    className.color.gammaCorrect(2.2)
    #                className.color = Color(255,255,255)
                    # put result into image
                    img[y][x] = className.color.toUINT8()
            else:
                hitSurface, hit_color, t, check = intersection_multiple(className, p, d)
                if(hit_color != ""):
                    # compute illumination at visible point
                    L = shading_multiple(className, p, d, t, hit_color, hitSurface)
                    className.color = Color(L[0], L[1], L[2])
                    className.color.gammaCorrect(2.2)
                    # put result into image
                    img[y][x] = className.color.toUINT8()
    return img

class Surface:
    def __init__(self, tree, root):
        self.tree = tree
        self.root = root

def main():
    tree = ET.parse(sys.argv[1])
    root = tree.getroot()

    imgSize=np.array(root.findtext('image').split()).astype(np.int)

    # Create an empty image
    channels=3
    img = np.zeros((imgSize[1], imgSize[0], channels), dtype=np.uint8)
    img[:,:]=0

    surface = Surface(tree, root)
    setDefault(surface)
    img = drawImg(surface, imgSize, img)

    rawimg = Image.fromarray(img, 'RGB')
    rawimg.save(sys.argv[1]+'.png')
    
if __name__=="__main__":
    main()
