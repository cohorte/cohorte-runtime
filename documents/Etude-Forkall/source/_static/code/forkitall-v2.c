/**
 * forkit.c
 *
 * @author Thomas Calmant <thomas dot calmant at isandlatech dot com>
 * @date 24/01/2011 (dd/mm/yy)
 */

#include <linux/compiler.h>
#include <linux/cpumask.h>
#include <linux/dcache.h>
#include <linux/fdtable.h>
#include <linux/fs.h>
#include <linux/kernel.h>
#include <linux/mount.h>
#include <linux/slab.h>
#include <linux/sched.h>
#include <linux/smp.h>
#include <linux/stop_machine.h>

#include <asm/current.h>
#include <asm/ptrace.h>
#include <asm/proto.h>
#include <asm/signal.h>
#include <asm/system.h>
#include <asm/unistd.h>

#include <../fs/internal.h>


/* From fork.c*/
extern long do_fork(unsigned long clone_flags, unsigned long stack_start,
        struct pt_regs *regs, unsigned long stack_size,
        int __user *parent_tidptr, int __user *child_tidptr);

/* Forced current variables */
struct task_struct* var_forced_current = 0;
struct task_struct* var_calling_current = 0;

/**
 * Forced current test (used by current.h)
 *
 * @param read_current *current* value that should be used if not forced
 *
 * @returns The forced *current* if needed, else the real *current*
 */
struct task_struct* forced_current(struct task_struct* read_current)
{
    if (read_current == var_calling_current)
        return var_forced_current;

    return read_current;
}
EXPORT_SYMBOL(forced_current);


/**
 * Counts the opened files in the given file descriptors table.
 * Copied from file.c (this method is not visible outside file.c)
 *
 * @param fdt a file descriptors table
 *
 * @returns The maximum amount of files opened in the fd table
 */
static int count_open_files__redef(struct fdtable *fdt)
{
    int size = fdt->max_fds;
    int i;

    /* Find the last open fd */
    for (i = size/(8*sizeof(long)); i > 0; ) {
        if (fdt->open_fds->fds_bits[--i])
            break;
    }
    i = (i+1) * 8 * sizeof(long);
    return i;
}

/**
 * Deeply copies a specific file structure
 *
 * @param filp The file to be copied
 *
 * @return The new file structure, 0 on error
 */
static struct file* copy_filp(struct file* filp)
{
    struct file* new_filp;

    // FIXME just a test...
    if(filp->private_data)
        return filp;

    /* Allocate the new file */
    new_filp = get_empty_filp();

    if(!new_filp)
    {
        printk(KERN_EMERG "Can't get empty filep\n");
        return 0;
    }

    /* Copy the file structure interesting values */
    if(filp->f_path.mnt)
        new_filp->f_path.mnt = mntget(filp->f_path.mnt);

    if(filp->f_path.dentry)
        new_filp->f_path.dentry = dget(filp->f_path.dentry);

    new_filp->f_flags = filp->f_flags;
    new_filp->f_mapping = filp->f_mapping;
    new_filp->f_mode = filp->f_mode;
    new_filp->f_pos = filp->f_pos;

    /* Used by TTY and sockets (contains a pointer to a socket structure) */
    new_filp->private_data = filp->private_data;

    memcpy(&new_filp->f_op, &filp->f_op, sizeof(struct file_operations*));
    memcpy(&new_filp->f_owner, &filp->f_owner, sizeof(struct fown_struct));

    return new_filp;
}

/**
 * Deeply copies the given file_struct : also copies the pointer content.
 *
 * @param files Pointer to the task files structures
 *
 * @returns 0 on success, < 0 on error.
 */
static int copy_files_struct(struct files_struct* files)
{
    int open_files, i;
    struct fdtable *fdt;

    /* Get the fd table */
    if(!files)
    {
        printk(KERN_EMERG "Invalid files structures");
        return -1;
    }

    /* Lock the files structure */
    spin_lock(&files->file_lock);

    fdt = files_fdtable(files);
    if(!fdt)
    {
        printk(KERN_EMERG "No file descriptors table");
        spin_unlock(&files->file_lock);
        return -2;
    }

    /* Get some informations */
    open_files = count_open_files__redef(fdt);

    /* Copy everything except standard I/O */
    for(i = open_files; i > 2; i--)
    {
        struct file* filp = fdt->fd[i];
        if(filp)
        {
            /* Copy the file structure */
            struct file* new_filp = copy_filp(filp);

            /* Replace the entry in the file table */
            rcu_assign_pointer(fdt->fd[i], new_filp);
        }
    }

    /* Unlock the files structure */
    spin_unlock(&files->file_lock);
    return 0;
}

/**
 * The forkitall core.
 *
 * @param pid (user-space) PID of the task to duplicate
 * @param others... As seen in do_fork()
 *
 * @returns the PID of the new task, 0 on error
 */
long do_forkit(unsigned long pid, unsigned long clone_flags,
        unsigned long stack_start, struct pt_regs *regs,
        unsigned long stack_size, int __user *parent_tidptr,
        int __user *child_tidptr)
{
    /* "p" in original code */
    struct task_struct* child_process;
    struct task_struct* parent_process;
    struct task_struct* calling_process;

    long current_status;
    long current_thread_status;
    long nr;

    long eax;
    struct pt_regs* thread_regs;

    struct thread_info* new_thread_info;
    struct thread_info* current_thread_info;

    struct task_struct* thread;
    struct task_struct* new_thread_task;
    long new_thread_id;
    int thread_clone_flags = (CLONE_VM | CLONE_FS | CLONE_FILES
            | CLONE_SIGHAND | CLONE_THREAD | CLONE_SYSVSEM
            /* | CLONE_SETTLS | CLONE_STOPPED */ );

    int nb_threads = 0;
    int i;
    long threads_status[112];


    /* Store the current task */
    calling_process = current;

    /* Retrieve the task_struct corresponding to the given pid */
    parent_process = find_task_by_vpid(pid);

    /* Flags tests skipped */

    if (parent_process == 0)
    {
        printk(KERN_EMERG "Get out : null pointer");
        return 0;
    }

    current_thread_info = task_thread_info(parent_process);
    stack_start = (unsigned long) parent_process->stack;

    /* PLATFORM DEPENDENT ! */
    regs = task_pt_regs(parent_process);
    eax = regs->ax;

    current_status = parent_process->state;
    // __set_task_state(parent_process, TASK_STOPPED);

    /* Set the parent as the current task */
    var_calling_current = calling_process;
    var_forced_current = parent_process;

    printk(KERN_EMERG "Fork");

    /* Copy the parent process */
    nr = do_fork(CLONE_PARENT /* | CLONE_STOPPED */, stack_start, regs,
            stack_size, parent_tidptr, child_tidptr);

    if (nr < 0)
    {
        printk(KERN_EMERG "MAIN FORK ERROR.");
    }
    else
    {
        child_process = find_task_by_vpid(nr);
        __set_task_state(child_process, TASK_STOPPED);

        printk(KERN_EMERG "Main fork done");

        /* Prepare to copy missing information */
        new_thread_info = task_thread_info(child_process);

        /* Hide the fact that the process has a signal pending */
        sigdelset(&child_process->pending.signal, SIGSTOP);
        clear_ti_thread_flag(new_thread_info, TIF_SIGPENDING);

        /* Reset the flags (remove TIF_FORK) */
        child_process->flags = parent_process->flags;

        /* Set the maximum valid memory */
        /* FIXME: may be null on some platform */
        new_thread_info->addr_limit = current_thread_info->addr_limit;

        /* Remove unusable flags (null value) */
        if(child_process->fs == 0) {
            thread_clone_flags &= ~CLONE_FS;
        }

        /*
         * Restore the child eax register,
         * modified by copy_thread in copy_process in do_fork
         */
        regs = task_pt_regs(child_process);
        regs->ax = eax;

        /* Go on with threads... */
        var_forced_current = child_process;

        /* Deep copy of the file descriptors table */
        {
            struct files_struct* child_files = get_files_struct(child_process);
            if(child_files == 0)
            {
                printk(KERN_EMERG "No valid files struct in the child");
            }
            else
            {
                copy_files_struct(child_files);
                put_files_struct(child_files);
            }
        }

        printk(KERN_EMERG "---------");
        printk(KERN_EMERG "Forking threads\n");

        thread = parent_process;
        while_each_thread(parent_process, thread)
        {
            if(thread == 0)
            {
                printk(KERN_EMERG "Thread %p, WTF ???\n", thread);
                break;
            }

            thread_regs = task_pt_regs(thread);
            if(thread_regs == 0)
            {
                printk(KERN_EMERG "NO REGS for thread %p\n", thread);
                continue;
            }

            /* Store eax */
            eax = thread_regs->ax;

            /* Store state */
            current_thread_status = thread->state;
            current_thread_info = task_thread_info(thread);

            /* Store & clear pending signals sets :
             * - parent_process->signal->shared_pending
             * - parent_process->pending
             */
            sigemptyset(&thread->pending.signal);

            /*
            if(thread->signal)
                sigemptyset(&thread->signal->shared_pending.signal);
            */

            /* Duplicate*/
            new_thread_id = do_fork(thread_clone_flags, thread_regs->sp,
                                    thread_regs, 0, 0, 0);

            /* Post-fork operations */
            if(new_thread_id > 0
                && (new_thread_task = find_task_by_vpid(new_thread_id)) != 0)
            {
                threads_status[nb_threads] = current_thread_status;
                __set_task_state(new_thread_task, TASK_STOPPED);

                new_thread_task->flags = child_process->flags;
                task_pt_regs(new_thread_task)->ax = eax;

                /* FIXME: may be null on some platform */
                new_thread_info = task_thread_info(new_thread_task);

                /* Set the maximum valid memory */
                new_thread_info->addr_limit = current_thread_info->addr_limit;

                nb_threads++;
                printk(KERN_EMERG "Thread %p forked -> %ld\n", thread, new_thread_id);
            }
            else
                printk(KERN_EMERG "THREAD %p FORK ERROR %ld\n", thread, -new_thread_id);
        }

        printk(KERN_EMERG "Finished forking threads\n");

        /* Restore the pending STOP signal */
        sigaddset(&child_process->pending.signal, SIGSTOP);
        set_tsk_thread_flag(child_process, TIF_SIGPENDING);

        /* Set the child status */
        __set_task_state(child_process, current_status);

        i = 0;
        while_each_thread(child_process, thread)
        {
            __set_task_state(thread, threads_status[i++]);
        }
    }

    /* Reset the *current* value */
    var_calling_current = 0;
    var_forced_current = 0;

    /* Reset the current task state */
    __set_task_state(parent_process, current_status);

    printk(KERN_EMERG "%d children have been treated.\n\n", nb_threads);
    return nr;
}
