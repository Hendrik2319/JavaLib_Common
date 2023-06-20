# JavaLib_Common
The `JavaLib_Common_*` libraries contains all code snippets, classes and concepts, I want to reuse over more than one project.  
This is originally the main part of these libraries.
All other `JavaLib_Common_*` libraries are extracts from `JavaLib_Common`.
At first and for a long time I've used SVN for versioning and these extracts were made via `SVN externals` (separate files or whole folders).
So all changes in the "extracted" files in the "extract" libraries were also made in the original files of `JavaLib_Common`.
The "extracted" files had the same full history as the original ones.

Now in GIT there is no similar mechanism like `SVN externals`, as far as I know.
So I've decided to make reduced copies of this `JavaLib_Common`.
All files in the "extract" libraries were removed from `JavaLib_Common` and vice versa,
that each file in all these libraries is exclusivly in only one library.

As a result, `JavaLib_Common` contains the complete history of all files, but only the least used files are left in the current state.
All other files were moved into the "extract" libraries, but without their history.

### Usage / Development
The current state of this libray is compatible with JAVA 17. But most of its code should be compatible with JAVA 8.  
This is an Eclipse project. `.classpath` and `.project` files are contained in the repository.  
If you want to use this library / clone this repository, you should clone all the other `JavaLib_Common_*` libraries too.
`JavaLib_Common` depends on all the other libraries. These dependencies were made via "project imports" in Eclipse.
So you should put all `JavaLib_Common_*` libraries into the same Eclipse workspace.
