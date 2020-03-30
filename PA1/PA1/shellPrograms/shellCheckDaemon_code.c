#include "shellPrograms.h"

/*  A program that prints how many summoned daemons are currently alive */
int shellCheckDaemon_code()
{

   /* TASK 8 */
   //Create a command that trawl through output of ps -efj and contains "summond"
   char *command = malloc(sizeof(char) * 256);
   sprintf(command, "ps -efj | grep summond  | grep -v tty > output.txt");
   // sprintf(command, "ps -efj | grep summond  | grep -v grep > output.txt");

   // TODO: Execute the command using system(command) and check its return value
   int status = system(command);
   if (status == -1) {
      return 1;
   }

   free(command);

   int live_daemons = 0;
   // TODO: Analyse the file output.txt, wherever you set it to be. You can reuse your code for countline program
   // 1. Open the file
   FILE *fp = fopen("output.txt", "r");
   if (fp == NULL) {
        perror("File failed to be opened.\n");
        return 1;
    }

   // 2. Fetch line by line using getline()
   char *buffer;
   size_t size = SHELL_BUFFERSIZE;
   buffer = (char*) malloc(size * sizeof(char));
   int checkline = getline(&buffer, &size, fp);

   // 3. Increase the daemon count whenever we encounter a line
   while (checkline != -1) {
      live_daemons++;
      printf("%s\n", buffer);
      checkline = getline(&buffer, &size, fp);
   }

   // 4. Close the file
   fclose(fp);

   // 5. print your result
   if (live_daemons == 0)
      printf("No daemon is alive right now\n");
   else
   {
      printf("There are in total of %d live daemons \n", live_daemons);
   }


   // TODO: close any file pointers and free any statically allocated memory 
   free(buffer);

   return 1;
}

int main(int argc, char **args)
{
   return shellCheckDaemon_code();
}