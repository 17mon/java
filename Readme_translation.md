IP.enableFileWatch = true; // The default value is: false, if it is true, it will check the changes of the ip library file and automatically reload the data

IP.load("The local absolute path of the IP library");

IP.find("8.8.8.8");//Returns an array of strings ["GOOGLE","GOOGLE"]


The usage of IPExt is the same as that of IP, but it is used to parse datx format files.
