package xyz.hyperreal


/**
 * Provides an abstract class for building and using B+ Trees.
 * 
 * ==Overview==
 * The class to extend is [[BPlusTree]].  It is designed to be both generic (type parameters for keys and values, and an abstract type for node references) and general (doesn't care how the tree is stored). An extending class needs to implement a number of simple methods and node type that 	provide storage abstraction.
 * 
 * There are two examples that extend `AbstractBPlusTree`: `MemoryBPlusTree` and `FileBPlusTree`. [[MemoryBPlusTree]] implements a B+ Tree in-memory and is essentially a map implementation.  [[FileBPlusTree]] implements a B+ Tree on-disk and is sort-of a very simple database.
 */
package object btree {}