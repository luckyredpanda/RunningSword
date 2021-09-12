# Backend

1. install WSL2, and Docker
2. Open the backend file in Visual Studio Code
3. Create a folder in root directory ，names ’uploads‘ with write permissions
4. run the following command 
 `docker-compose up`  /  `docker-compose -d`



**IMPORTANT**: `start.sh` must have LF line endings. Sometimes when cloning/copying on different systems this will be set to CRLF or CR. Then the installation will fail! Also `start.sh` needs `chmod +x start.sh`.

The commands from `start.sh` can also be run manually in the CLI of express service.

If ws service is exiting try to run `docker-compose up express` first, then stop it when it is done and run `docker-compose up` again.