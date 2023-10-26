# Gitlet Design Document
author: Nora Povejsil

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy. 
###Main:
It implements methods to set up persistence and support each command in the program. It runs the program. 
Calling methods to run the program from component classes. 

### Command:
This class holds all the commands for gitlet and the algorithms for each of these methods. 
####Fields 
1. static final File CWD: a pointer to the current working directory
2. static final File GITLET_FOLDER: A pointer to the the gitlet directory
3. static final File COMMIT_DIR: in the gitlet folder, contains committed files
### Staging
Hashes the contents of objects to be stored after calling .add(). Adds files to staging area to be committed next time a commit is made. Is cleared after each commit. 
### Commit 
This takes information from the Staging class and stores it persistently so that we can access hashed files for each commit.
####Fields
1. private String message: string representing commit message (-m)
2. private String timestamp: string representing time of commit 
3. private Object trackedFiles: objects to keep track of for each commit (changed files)



## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.
### Main.java
1. main(String[] args): This is the entry point of the program.

### Commit.java
1. makeCommit()
   * append new blobs to existing treemap
   * new additions make up new commit (to store)
   * match a pointer to a blob in an arraylist??? I want to a commit to reference any relevant blobs. 
   * making an object with the id and the file to be committed. 
### CommandClass.java
1. init(): 
   * Creates a new Gitlet version-control system in the current directory.
2. add()
   * Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
3. commit()
   * Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
4. log()
   * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
5. checkout()
  * Checkout is a general command that can do a few different things depending on what its arguments are.
    * Write three versions with the same name but that have different input format
      * java gitlet.Main checkout -- [file name]
      * java gitlet.Main checkout [commit id] -- [file name]
      * java gitlet.Main checkout [branch name]
6. rm()
   * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit). 
7. globalLog()
  * Like log, except displays information about all commits ever made. The order of the commits does not matter.
8. find()
   * Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines.
9. status()
   * Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. An example of the exact format it should follow is as follows.
10. branch()
  * Creates a new branch with the given name, and points it at the current head node. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
11. rmBranch()
    * Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
12. reset()
  * Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node. See the intro for an example of what happens to the head pointer after using reset.
13. merge()
  * Merges files from the given branch into the current branch. This method is a bit complicated, so here's a more detailed description:

### Staging.java
1. addToStage():
   * files to change
   * file to blob 
   * store blobs in a treemap
3. clearStage():
   * reset treemap to empty (called after commit)


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.
#### Making git object and saving 
* make git object at the beginning 
* call all methods on that object 
* read everything from previous commits at the beginning of Main 
* at the end of main, serialize and save this new object to directories

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

![](../../../../../var/folders/2f/8nz88zms4bg9vsltw872nfjm0000gn/T/TemporaryItems/NSIRD_screencaptureui_zNsjcB/Screen Shot 2022-04-14 at 12.39.38 PM.png)