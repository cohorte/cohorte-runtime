/*
 * syscall.c
 *
 *  Created on: 16 févr. 2011
 *  Updated on:  3 mars  2011
 *      Author: Thomas Calmant
 */
#include <signal.h>
#include <stdio.h>
#include <sys/syscall.h>
#include <unistd.h>

int main(int argc, char** argv)
{
	unsigned long pid;
	long result;

	if(argc != 2) {
		printf("Usage: %s pid\n", argv[0]);
		return 1;
	}

	pid = atol(argv[1]);

	printf("Stop the target...");
	kill(pid, SIGSTOP);

	usleep(50);

	printf("Syscall on %lu...\n", pid);
	result = syscall(300, pid);
	printf("Result : %lu\n", result);

	printf("Restart processes...");
	kill(pid, SIGCONT);
	kill(result, SIGCONT);
	printf("Done\n");
	return result;
}
