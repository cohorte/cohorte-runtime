/*
 * main.c
 *
 *  Created on: 24 févr. 2011
 *      Author: Thomas Calmant
 */
#include <signal.h>
#include <sys/signal.h>

void handler(int sig, siginfo_t* info, void* context)
{
	int (*jmp)(void);
	char buf[256];

	/* Exécution à l'adresse donnée */
	read(fd[0], buf, sizeof(buf));
	sscanf(buf, "%d", jmp);
	jmp();
}

int main(int argc, char** argv)
{
	struct sigaction action;

	// ...

	/* Gestionnaire de signal */
	// Nécessaire pour ajouter les paramètres siginfo
	action.sa_flags = SA_SIGINFO;

	// Pas de masquage de signal
	memset(&action.sa_mask, 0, sizeof(sigset_t));

	// Gestionnaire
	action.sa_sigaction = handler;

	sigaction(SIGUSR1, &action, 0);

	// ...
}
