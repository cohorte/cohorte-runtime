/*
 * syscall.c
 *
 *  Created on: 16 févr. 2011
 *      Author: Thomas Calmant
 */
#include <stdio.h>
#include <sys/syscall.h>

int main(int argc, char** argv)
{
	unsigned long pid;
	long result;

	if(argc != 2) {
		printf("Usage: %s pid\n", argv[0]);
		return 1;
	}

	pid = atol(argv[1]);

	printf("Syscall on %lu...\n", pid);
	result = syscall(300, pid);
	printf("Result : %lu\n", result);
	return result;
}
