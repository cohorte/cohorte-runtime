/*
 * main.c
 *
 *  Created on: 24 févr. 2011
 *      Author: Thomas Calmant
 */

#include <stdio.h>
#include <unistd.h>

// Dans .data_noinit (en théorie)
char ptr;

// Dans .data_static (en théorie)
char ptr2 = 0;

int main(int argc, char** argv)
{
	char* i;
	char* c;
	int nb_shown = 0;

	printf("PID : %x (%d)\n i : %p ; ptr : %p ; ptr2 : %p ; main : %p\n",
	        getpid(), getpid(), &i, &ptr, &ptr2, main);

	// Lecture de l'en-tête du fichier ELF
	//	for (i = &ptr - 4144; i < &ptr; i++)
	//	{
	//		printf("%x", *i);
	//	}


	// Lecture de la pile
	for (c = &i;; c++, nb_shown++)
	{
		if (!(nb_shown % 4))
			printf("\n%p\t", &i + nb_shown);

		printf("%08x ", *c);
	}
	printf("\n");

	return 0;
}
