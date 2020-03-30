# 50005Lab1
Short segment on how TODO#4 in 50.005 Lab 1 is tackled

## TODO#4

[Pseudocode for mainloop]

// Create job, busy wait
while the file still has input:
	while (true):
		if (job for child i is completed or no job, i.e. status = 0):
			if (child i is alive):
				assign new job to job_buffer[i];
				increment semaphore[i] for child to continue with job;
				break (inner loop, move to next file input);
			else (child i is dead):
				revive child using fork();
				dispatch child;
				register new pid of child in children_processes[i];
				assign new job to job_buffer[i];
				increment semaphore[i] for child to continue with job;
				break (inner loop, move to next file input);
		else (job is still running or terminated, i.e. status = 0):
			if (child i is dead, i.e. job was terminated):
				revive child using fork();
				dispatch child;
				register new pid of child in children_processes[i];
				assign new job to job_buffer[i];
				increment semaphore[i] for child to continue with job;
				break (inner loop, move to next file input);
			else (child i is alive):
				// do nothing because job is still running
		increment (and modulus num_of_processes) i to check next jobs buffer
close file;
// Terminate child processes that are alive
for (i in processes):
	if (child i is alive):
		continue (skip this loop);
	if (task_type is t or w):
		// Busy wait if job is still running
		while (task_status != 0):
			check task_status again;
		assign task_type 'z'
		assign task_duration 0;
		assign task_status 1 (IMPORTANT!);
		increment semaphore[i] for child to continue with job;
	else (task_type is z or i):
		// Do nothing because child will terminate on its own
// Wait for all children processes to execute 'z' termination job
... (Code is given)
// Print final results
... (Code is given)


[Additional Details]

Challenges encountered:
1. Stuck in main loop forever 
	* I did not assign task_status to 1 in termination code
	* job_dispatch works such that it will semaphore-wait until status is 1 for new job
	* Code in main loop waited for the child process to terminate but never did
2. Stuck in main loop forever #2
	* Forgot to increase semaphore in termination code after assigning z01 to each child
	* Child waited forever for semaphore to increment before it can do termination job







