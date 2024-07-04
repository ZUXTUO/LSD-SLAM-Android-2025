## LSD-SLAM: Large-Scale Direct Monocular SLAM ANDROID

- You can download the APK(Experimental) and test it right away.
- I modified an old project to make it usable today (2024).
- This project is licensed under the GPL 3.0 license.<br><br>


README of the Original Project
---
forked: https://github.com/omair18/LSD-SLAM-Android <br>

LSD-SLAM is a novel approach to real-time monocular SLAM. It is fully direct (i.e. does not use keypoints / features) and creates large-scale, 
semi-dense maps in real-time on a laptop. For more information see
[http://vision.in.tum.de/lsdslam](http://vision.in.tum.de/lsdslam)
where you can also find the corresponding publications and Youtube videos, as well as some 
example-input datasets, and the generated output as rosbag or .ply point cloud.

This fork contains Android Version of LSD SLAM, including the JNI files used by https://github.com/striversist/LSDDemo. 

Android application has been forked from https://github.com/striversist/LSDDemo. 

All dependencies & libraries have been included in JNI folder. I have also placed the NDK compiled version of g2o library (for armeabi & armebi-v7a archs). 

I have used ndk-14b for the compilation. 

#Recommended: 
Run ndk-build inside JNI folder before running this app to get fresh compiled libraries.. 

#Note:
All credit goes to authors of lsdslam. My contribution is only related to compilation for Android platform. 
