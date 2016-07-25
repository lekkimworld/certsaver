# Project Description
This is a simple Java command to save the certificate chain from any HTTPS server to PEM files. The files are saved in the same directory the command is invoked from. 

# Compiling
To compile do as follows:
1. Clone the repo
2. Change into the src/java/main directory `cd src/java/main`
3. Compile the code `javac Main.java`
4. Test the command `java Main`

# Running
To run do as follows:
1. Change to the directory you compiled the source in
2. Run specifying the hostname of the server `java Main https://www.google.com`

# Disclaimer
Error checking is simple to non-existing. Use at your own risk.
