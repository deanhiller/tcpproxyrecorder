This folder is where jars go that 
1. Are needed for the application to run.  
2. need to be packaged with the application

For normal applications, jars in this directory will be delivered along 
with your jar to output/jardist directory

For web applications, jars in this directory will be put in the war file.
For osgi bundles, jars in this diretory will be put in the osgi bundle

Examples of jars that don't belong in this directory are 
junit.jar, mocklib.jar, osgi's framework.jar, servlet.jar, etc. etc.