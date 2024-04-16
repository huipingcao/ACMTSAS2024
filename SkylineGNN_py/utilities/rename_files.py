import os
import sys
 
# Function to rename multiple files
def main():
   

    folder = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_bbs/C9_NY_NONE_5K_TSP_L1-6_train_val_9000"

    for _, filename in enumerate(os.listdir(folder)):

#        dst = f"{filename[:-11]}_TSP_L1-6.log"
        dst = f"{filename[:-24]}_TSP_L1-6.log"
#        print(dst)

        src =f"{folder}/{filename}"  # foldername/filename, if .py file is outside folder
        dst =f"{folder}/{dst}"
#        print(dst)

#        sys.exit()
      
        os.rename(src, dst)
 

if __name__ == '__main__':
     
    main()
