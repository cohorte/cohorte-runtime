/*
 * lib.c
 *
 *  Created on: 30 janv. 2012
 *      Author: Thomas Calmant
 */

#include "lib.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifndef UPDATED

int read_state(char* file_name)
{
	FILE* fd;
	char state;
	int state_val;

	fd = fopen(file_name, "r");
	if(!fd)
		return -1;

	if(fread(&state, sizeof(char), 1, fd) == sizeof(state))
	{
		state_val = (int) state - '0';
		if(state_val < 0 || state_val > 5)
			state_val = -2;
	}
	else
		state_val = -3;

	fclose(fd);
	return state_val;
}

#else

int read_state(char* file_name)
{
	FILE* fd;
	char* state;
	long fd_len;
	int state_val;

	fd = fopen(file_name, "r");
	if(!fd)
		return -1;

	fseek(fd, 0, SEEK_END);
	fd_len = ftell(fd);
	fseek(fd, 0, SEEK_SET);

	state = (char*) malloc(fd_len + 1);
	memset(state, 0, fd_len + 1);

	if(fread(state, fd_len, 1, fd) >= 0)
	{
		sscanf(state, "%d", &state_val);
		if(state_val < 0 || state_val > 100)
			state_val = -2;
	}
	else
		state_val = -3;

	fclose(fd);
	free(state);
	return state_val;
}

#endif
