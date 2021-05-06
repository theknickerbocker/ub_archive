# FixationDataAnlyzer
Program takes fixation time stamps and locations and measures each user's data using certain gaze-metrics. 
Originally created for research done at the Rochester Institute of Technology to monitor the fixation patterns of 
people who are deaf or hard of hearing while they watched videos with automated speech recognition-generated captioning.

I apoligize for sparse comments, during the writing of this code there were multiple approaching deadlines so I was pressed for time. Also fixation data must be obtained from the program "FixationDataRecorder" (another repo on github accessible from my profile)

Compilation:  
1) Navigate to directory ".../FixationDataAnalyzer" in the terminal  
2) Enter the following command:  
javac fix_data/Analyzer.java fix_data/AOI.java fix_data/DataPair.java fix_data/Driver.java fix_data/Fixation.java fix_data/Main.java fix_data/Style.java fix_data/User.java fix_data/Video.java   

Running:  
1) Navigate to directory ".../FixationDataAnalyzer" in the terminal  
2) Enter the following command ([] denote arguments to be entered):  
java fix_data.Main [number of videos per user] [number of users] [path to directory of video timestamps] [path to directory of fixation data] [path to csv file holding video order] [path to directory where output files are to be held]
