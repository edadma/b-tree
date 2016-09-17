package xyz.hyperreal.btree

import scala.sys.process._
import collection.mutable.{HashMap, ArrayBuffer}
import collection.immutable.ListMap

import java.io.PrintWriter


/**
 * Provides for interaction (searching, insertion, update, deletion) with a B+ Tree that can be stored any where (in memory, on disk).  It is the implementation's responsability to create the empty B+ Tree initially.  An empty B+ Tree consists of a single empty leaf node as the root.  Also the `first` and `last` should refer to the root leaf node, and the `lastlen` variable should be 0.
 */
abstract class AbstractBPlusTree[K <% Ordered[K], +V]( order: Int ) {
	/**
	 * Abstract node type. For in-memory implementations this would probably be the actual node class and for on-disk it would likely be the file pointer where the node is stored.
	 */
	protected type N
	
	/**
	 * Root node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations). The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var root: N
		
	/**
	 * First leaf node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations) within the implementation's `newRoot` method. The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var first: N
		
	/**
	 * Last leaf node. Implementations are required to set this as well as to create/update the in-storage copy of this if needed (only really applies to on-disk implementations). The methods in this class will take care of updating this variable, implementations only need to worry about the in-storage copy.
	 */
	protected var last: N
		
	/**
	 * Length of the last leaf node.  This just speeds up bulk loading (the `load` method). Implementations are required to set this.
	 */
	protected var lastlen: Int
	
	/**
	 * Frees that storage previously allocated for `node`. For in-memory implementations, this method probably won't do anything.
	 */
	protected def freeNode( node: N )
		
	/**
	 * Gets a branch pointer from an internal node at a given `index`.  There is always one more branch pointer than there are keys in an internal node so the highest index is equal to `nodeLength( node )`.
	 */
	protected def getBranch( node: N, index: Int ): N
	
	/**
	 * Gets the branches of `node` as a non-strict immutable sequence.
	 */
	protected def getBranches( node: N ): Seq[N]
		
	/**
	 * Gets a key from a leaf `node` at a given `index`.
	 */
	protected def getKey( node: N, index: Int ): K
	
	/**
	 * Gets the keys of `node` as a non-strict immutable sequence.
	 */	
	protected def getKeys( node: N ): Seq[K]
	
	/**
	 * Returns the next pointer of (leaf) `node`.
	 */
	protected def getNext( node: N ): N
	
	/**
	 * Returns the parent pointer of `node`.
	 */
	protected def getParent( node: N ): N
	
	/**
	 * Returns the previous leaf node link pointer of (leaf) `node`.
	 */
	protected def getPrev( node: N ): N
		
	/**
	 * Gets a value from a leaf `node` at a given `index`.
	 */
	protected def getValue( node: N, index: Int ): V
	
	/**
	 * Gets the values of `node` as a non-strict immutable sequence.
	 */	
	protected def getValues( node: N ): Seq[V]
	
	/**
	 * Inserts `key` and `branch` into (internal) `node` at `index`. The branch is the right branch immediately to the right of `key`.
	 */
	protected def insertInternal( node: N, index: Int, key: K, branch: N )
	
	/**
	 * Inserts `key` and `value` into (leaf) `node` at `index`.
	 */
	protected def insertLeaf[V1 >: V]( node: N, index: Int, key: K, value: V1 )
	
	/**
	 * Returns `true` if `node` is a leaf node
	 */
	protected def isLeaf( node: N ): Boolean
	
	/**
	 * Moves key/branch pairs as well as the left branch of the first key from node `src` to node `dst` beginning at index `begin` and ending up to but not including index `end`, and also removes the key at index `begin - 1`.
	 */
	protected def moveInternal( src: N, begin: Int, end: Int, dst: N )
	
	/**
	 * Moves key/value pairs from node `src` to node `dst` beginning at index `begin` and ending up to but not including index `end`.
	 */
	protected def moveLeaf( src: N, begin: Int, end: Int, dst: N, index: Int )
	
	/**
	 * Creates a new internal node with `parent` as its parent pointer.
	 */
	protected def newInternal( parent: N ): N
	
	/**
	 * Creates a new leaf node with `parent` as its parent pointer.
	 */
	protected def newLeaf( parent: N ): N
	
	/**
	 * Creates a new root (internal) node with `branch` as its leftmost branch pointer and `null` parent pointer. Implementations are require to update the in-storage copy of the root pointer if needed (only really applies to on-disk implementations).
	 */
	protected def newRoot( branch: N ): N
	
	/**
	 * Returns the length (number of keys) of `node`. For internal nodes, the number of branch pointers will one more than the length.
	 */
	protected def nodeLength( node: N ): Int
	
	/**
	 * Returns the ''null'' node pointer. For in-memory implementations this will usually be a Scala `null` value. For on-disk it would make sense for this to be `0L`.
	 */
	protected def nul: N

	/**
	 * Removes the key/branch pair from internal `node` at `index`. The branch that is removed is the one to the right of the key being removed, i.e. the branch at (`index` + 1). This method is perhaps poorly named: it does not remove an internal node from the tree.
	 * 
	 * @return length of `node` after removal
	 */
	protected def removeInternal( node: N, index: Int ): Int

	/**
	 * Removes the key/value pair from leaf `node` at `index`. This method is perhaps poorly named: it does not remove a leaf node from the tree.
	 * 
	 * @return length of `node` after removal
	 */
	protected def removeLeaf( node: N, index: Int ): Int
	
	/**
	 * Sets the in-storage copy of the first leaf node pointer. This method is not responsable for setting the `first` variable.
	 */
	protected def setFirst( leaf: N )

	/**
	 * Sets the key at `index` of `node` to `key`.
	 */
	protected def setKey( node: N, index: Int, key: K )

	/**
	 * Sets the in-storage copy of the last leaf node pointer. This method is not responsable for setting the `last` variable nor the `lastlen` variable.
	 */
	protected def setLast( leaf: N )
	
	/**
	 * Sets the next pointer of (leaf) `node` to `p`.
	 */
	protected def setNext( node: N, p: N )
	
	/**
	 * Sets the parent pointer of `node` to `p`.
	 */
	protected def setParent( node: N, p: N )
	
	/**
	 * Sets previous leaf node link pointer of (leaf) `node` to `p`.
	 */
	protected def setPrev( node: N, p: N )
	
	/**
	 * Sets the in-storage copy of the root node pointer. This method is not responsable for setting the `root` variable.
	 */
	protected def setRoot( node: N )
	
	/**
	 * Sets the value at `index` of `node` to `v`.
	 */
	protected def setValue[V1 >: V]( node: N, index: Int, v: V1 )
	
	
	/**
	 * Returns a bounded iterator over a range of key/value pairs in the tree in sorted order. The range of key/value pairs in the iterator is specified by `bounds`.  `bounds` must contain one or two pairs where the first element in the pair is a symbol corresponding to the type of bound (i.e. '<, '<=, '>, '>=) and the second element is a key value.
	 * 
	 * An example of a bounded iterator over all elements in a tree (with `String` keys) that will include all keys that sort greater than or equal to "a" and up to but not including "e" is `boundedIterator( ('>=, "a"), ('<, "e") )`.
	 */
	def boundedIterator( bounds: (Symbol, K)* ): Iterator[(K, V)] = boundedPositionIterator( bounds: _* ) map {case (n, i) => getKeyValue( n, i )}

	/**
	 * Returns a bounded iterator over a range of key positions (node/index pairs) in the tree in sorted order. The `bounds` parameter is the same as for [[boundedIterator]].
	 */
	protected def boundedPositionIterator( bounds: (Symbol, K)* ): Iterator[(N, Int)] = {
		def gte( key: K ) =
			lookupGTE( key ) match {
				case (_, leaf, index) => (leaf, index)
			}

		def gt( key: K ) =
			lookupGTE( key ) match {
				case (true, leaf, index) => nextPosition( leaf, index )
				case (false, leaf, index) => (leaf, index)
			}

		require( bounds.length == 1 || bounds.length == 2, "boundedIterator: one or two bounds" )
			
		val symbols = ListMap[Symbol, K => (N, Int)]( '> -> gt, '>= -> gte, '< -> gte, '<= -> gt )
		
		require( bounds forall {case (s, _) => symbols contains s}, "boundedIterator: expected one of '<, '<=, '>, '>=" )
		
		def translate( bound: Int ) = symbols(bounds(bound)._1)(bounds(bound)._2)
		
		def order( s: Symbol ) = symbols.keys.toList indexOf s
		
		val ((loleaf, loindex), (upleaf, upindex)) =
			if (bounds.length == 2) {
				require( bounds(0)._1 != bounds(1)._1, "boundedIterator: expected bounds symbols to be different" )

				val (lo, hi, (slo, klo), (shi, khi)) =
					if (order( bounds(0)._1 ) > order( bounds(1)._1 ))
						(translate( 1 ), translate( 0 ), bounds(1), bounds(0))
					else
						(translate( 0 ), translate( 1 ), bounds(0), bounds(1))
						
				if (klo > khi || klo == khi && ((slo != '>=) || (shi != '<=)))
					(lo, lo)
				else
					(lo, hi)
			} else if (order( bounds(0)._1 ) < 2)
				(translate( 0 ), (nul, 0))
			else
				((first, 0), translate( 0 ))
			
		new Iterator[(N, Int)] {
			var leaf: N = loleaf
			var index: Int = loindex

			def hasNext = leaf != nul && index < nodeLength( leaf ) && (leaf != upleaf || index < upindex)

			def next =
				if (hasNext) {
					val (n, i) = nextPosition( leaf, index )
					val cur = (leaf, index)
							
					leaf = n
					index = i
					cur
				}
				else
					throw new NoSuchElementException( "no more keys" )
		}
	}

	/**
	 * Returns a bounded iterator over a range of keys in the tree in sorted order. The `bounds` parameter is the same as for [[boundedIterator]].
	 */
	def boundedKeysIterator( bounds: (Symbol, K)* ) = boundedPositionIterator( bounds: _* ) map {case (n, i) => getKey( n, i )}

	/**
	 * Returns a bounded iterator over a range of values in the tree in sorted order. The `bounds` parameter is the same as for [[boundedIterator]].
	 */
	def boundedValuesIterator( bounds: (Symbol, K)* ) = boundedPositionIterator( bounds: _* ) map {case (n, i) => getValue( n, i )}
	
	/**
   * Returns `true` is the tree is empty.
   */
	def isEmpty = lastlen == 0
	
	/**
   * Returns an iterator over all key/value pairs in the tree in sorted order.
   */
	def iterator = positionIterator map {case (n, i) => getKeyValue( n, i )}
	
	/**
   * Returns an iterator over all key/value pairs in the tree in sorted order.
   */
	def reverseIterator = reversePositionIterator map {case (n, i) => getKeyValue( n, i )}

	/**
   * Returns an iterator over all key positions (node/index pairs) in the tree in ascending sorted order.
   */
	protected def positionIterator: Iterator[(N, Int)] =
		new Iterator[(N, Int)] {
			var leaf = first
			var index = 0
			
			def hasNext = leaf != nul && index < nodeLength( leaf )
			
			def next =
				if (hasNext) {
					val (n, i) = nextPosition( leaf, index )
					val cur = (leaf, index)
							
					leaf = n
					index = i
					cur
				} else
					throw new NoSuchElementException( "no more keys" )
		}

	/**
   * Returns a reverse iterator over all key positions (node/index pairs) in the tree in descending sorted order.
   */
	protected def reversePositionIterator: Iterator[(N, Int)] =
		new Iterator[(N, Int)] {
			var leaf = last
			var index = lastlen - 1
			
			def hasNext = leaf != nul && index >= 0
			
			def next =
				if (hasNext) {
					val (n, i) = prevPosition( leaf, index )
					val cur = (leaf, index)
							
					leaf = n
					index = i
					cur
				} else
					throw new NoSuchElementException( "no more keys" )
		}

  /**
   * Returns an iterator over all keys in the tree in ascending sorted order.
   */
	def keysIterator = positionIterator map {case (n, i) => getKey( n, i )}

  /**
   * Returns a reverse iterator over all keys in the tree in descending sorted order.
   */
	def reverseKeysIterator = reversePositionIterator map {case (n, i) => getKey( n, i )}
	
	/**
	 * Returns the maximum key and it's associated value.
	 * 
	 * @return `Some( (key, value) )` where `key` is the maximum key and `value` is it's associated value if the tree is non-empty, or `None` if the tree is empty.
	 */
	def max =
		if (isEmpty)
			None
		else
			Some( (getKey(last, lastlen - 1), getValue(last, lastlen - 1)) )
	
	/**
   * Returns the maximum key.
   */
  def maxKey =
    if (isEmpty)
      None
    else
      Some( getKey(last, lastlen - 1) )
    
	/**
	 * Returns the minimum key and it's associated value.
	 * 
	 * @return `Some( (key, value) )` where `key` is the minimum key and `value` is it's associated value if the tree is non-empty, or `None` if the tree is empty.
	 */
	def min =
		if (isEmpty)
			None
		else
			Some( (getKey(first, 0), getValue(first, 0)) )
  
  /**
   * Returns the minimum key.
   */
  def minKey =
    if (isEmpty)
      None
    else
      Some( getKey(first, 0) )
  
  /**
   * Inserts `key` with associated `value` into the tree. If `key` exists, then it's new associated value will be `value`.
   * 
   * @return `true` if `key` exists
   */
  def insert[V1 >: V]( key: K, value: V1 = null.asInstanceOf[V1] ) =
    lookup( key ) match {
      case (true, leaf, index) =>
        setValue( leaf, index, value )
        true
      case (false, leaf, index) =>
        insertAt( key, value, leaf, index )
        false
    }
	
  /**
   * Inserts `key` with associated `value` into the tree only if `key` does not exist.
   * 
   * @return `true` if `key` exists
   */
	def insertIfNotFound[V1 >: V]( key: K, value: V1 = null.asInstanceOf[V1] ) =
		lookup( key ) match {
			case (true, _, _) => true
			case (false, leaf, index) =>
				insertAt	( key, value, leaf, index )
				false
		}
	
	/**
	 * Inserts `keys` into the tree each with an associated value of `null`. If a given key exists, then it's new associated value will be `null`.
	 */
	def insertKeys( keys: K* ) =
		for (k <- keys)
			insert( k, null.asInstanceOf[V] )
	
	/**
	 * Inserts `keys` into the tree each with an associated value of `null`, and checks that the tree is well constructed after each key is inserted. If a given key exists, then it's new associated value will be `null`. This method is used for testing.
	 */
	def insertKeysAndCheck( keys: K* ): String = {
		for (k <- keys) {
			insert( k, null.asInstanceOf[V] )
			
			wellConstructed match {
				case "true" =>
				case reason => return reason
			}
		}
		
		"true"
	}
	
	/**
   * Performs the B+ Tree bulk loading algorithm to insert key/value pairs `kvs` into the tree efficiently. This method is more efficient than using `insert` because `insert` performs a search to determine the correct insertion point for the key whereas `load` does not. `load` can only work if the tree is empty, or if the minimum key to be inserted is greater than the maximum key in the tree.
   */
	def load[V1 >: V]( kvs: (K, V1)* ) {
		require( !kvs.isEmpty, "expected some key/value pairs to load" )
		
		val seq = kvs sortBy {case (k, _) => k}
		
		maxKey match {
			case None =>
			case Some( maxkey ) =>
				if (maxkey >= seq.head._1)
					sys.error( "can only load into non-empty tree if maximum element is less than minimum element to be loaded" )
		}
		
		seq foreach {case (k, v) => insertAt( k, v, last, lastlen )}
	}
	
	/**
	 * Searches for `key` returning it's associated value if `key` exists.
	 * 
	 * @return `Some( value )` where `value` is the value associated to `key` if it exists, or `None` otherwise
	 */
	def search( key: K ): Option[V] =
		lookup( key ) match {
			case (true, leaf, index) => Some( getValue(leaf, index) )
			case _ => None
		}

		
	/**
	 * Performs a binary search for key `target` within `node` (tail recursively).
	 * 
	 * @return the index of `target` within `node` if it exists, or (-''insertionPoint'' - 1) where ''insertionPoint'' is the index of the correct insertion point for key `target`.
	 */
	protected def binarySearch( node: N, target: K ): Int = {
		def search( start: Int, end: Int ): Int = {
			if (start > end)
				-start - 1
			else {
				val mid = start + (end-start + 1)/2
				
				if (getKey( node, mid ) == target)
					mid
				else if (getKey(node, mid) > target)
					search( start, mid - 1 )
				else
					search( mid + 1, end )
			}
		}
		
		search( 0, nodeLength(node) - 1 )
	}
	
	/**
	 * Performs the B+ Tree lookup algorithm (tail recursively) beginning at the root, in search of the location (if found) or correct insertion point (if not found) of `key`.
	 * 
	 * @return a triple where the first element is `true` if `key` exists and `false` otherwise, the second element is the node containing `key` if found or the correct insertion point for `key` if not found, the third is the index within that node.
	 */
	protected def lookup( key: K ): (Boolean, N, Int) = {
		def _lookup( n: N ): (Boolean, N, Int) =
			if (isLeaf( n ))
				binarySearch( n, key ) match {
					case index if index >= 0 => (true, n, index)
					case index => (false, n, -(index + 1))
				}
			else
				binarySearch( n, key ) match {
					case index if index >= 0 => _lookup( getBranch(n, index + 1) )
					case index => _lookup( getBranch(n, -(index + 1)) )
				}
			
		_lookup( root )
	}
	
	/**
	 * Returns the key/value pair at `index` in `leaf` as well as the leaf node and index where the next key (in sorted order) is located. This method assumes that `index` is the index of an existing key within `node`.
	 * 
	 * @return a pair where the first element is the key/value pair, and the second element is a pair containing the node containing the next key in sorted order (or `null` if there is no next key), and the index of the next key (or 0 if there is no next key)
	 */
	protected def nextKeyValue( leaf: N, index: Int ) = (getKeyValue( leaf, index), nextPosition( leaf, index ))
	
	/**
	 * Returns the key/value pair at `index` within `leaf`.
	 */
	protected def getKeyValue( leaf: N, index: Int ) = (getKey( leaf, index ), getValue( leaf, index ))
	
	/**
	 * Returns the node/index pair pointing to the location of the leaf node key preceding the one at `index` in `leaf`.
	 */
	protected def prevPosition( leaf: N, index: Int ) =
		if (index == 0) {
			val prev = getPrev( leaf )
			
			(prev, if (prev == nul) 0 else nodeLength( prev ) - 1)
		} else
			(leaf, index - 1)
	
	/**
	 * Returns the node/index pair pointing to the location of the leaf node key following the one at `index` in `leaf`.
	 */
	protected def nextPosition( leaf: N, index: Int ) =
		if (index == nodeLength( leaf ) - 1)
			(getNext( leaf ), 0)
		else
			(leaf, index + 1)
	
	/**
	 * Searches for `key` returning a point in a leaf node that is the least greater than (if not found) or equal to (if found) `key`. The leaf node and index returned in case `key` does not exist is not necessarily the correct insertion point. This method is used by `boundedIterator`.
	 * 
	 * @return a triple where the first element is `true` if `key` exists and `false` otherwise, and the second element is the leaf node containing the least greater than or equal key, and the third is the index of that key.
	 */
	protected def lookupGTE( key: K ) =
		lookup( key ) match {
			case t@(true, _, _) => t
			case f@(false, leaf, index) =>
				if (index < nodeLength( leaf ))
					f
				else
					(false, getNext( leaf ), 0)
		}
	
	/**
	 * Performs the B+ Tree insertion algorithm to insert `key` and associated `value` into the tree, specifically in `leaf` at `index`, rebalancing the tree if necessary. If `leaf` and `index` is not the correct insertion point for `key` then this method will probably result in an invalid B+ Tree.
	 */
	protected def insertAt[V1 >: V]( key: K, value: V1, leaf: N, index: Int ) {
		def split = {
			val newleaf = newLeaf( getParent(leaf) )
			val leafnext = getNext( leaf )
			
			setNext( newleaf, leafnext )
			
			if (leafnext == nul) {
				last = newleaf
				setLast( newleaf )
			} else
				setPrev( leafnext, newleaf )
				
			setNext( leaf, newleaf )
			setPrev( newleaf, leaf )
			
			val len = nodeLength( leaf )
			val mid = len/2
			
			if (leafnext == nul)
				lastlen = len - mid
				
			moveLeaf( leaf, mid, len, newleaf, 0 )
			newleaf
		}
		
		insertLeaf( leaf, index, key, value )
		
		if (leaf == last)
			lastlen += 1

		if (nodeLength( leaf ) == order) {
			if (getParent( leaf ) == nul) {
				root = newRoot( leaf )
				setParent( leaf, root )
				
				val newleaf = split
				
				insertInternal( root, 0, getKey(newleaf, 0), newleaf )
			} else {
				var par = getParent( leaf )
				val newleaf = split
				
				binarySearch( par, getKey(newleaf, 0) ) match {
					case index if index >= 0 => sys.error( "key found in internal node" )
					case insertion =>
						insertInternal( par, -(insertion + 1), getKey(newleaf, 0), newleaf )
				
						while (nodeLength( par ) == order) {
							val newinternal = newInternal( getParent(par) )
							val len = nodeLength( par )
							val mid = len/2
							val middle = getKey( par, mid )
							
							moveInternal( par, mid + 1, len, newinternal )
							
							for (child <- getBranches( newinternal ))
								setParent( child, newinternal )
								
							if (getParent( par ) == nul) {
								root = newRoot( par )
								
								setParent( newinternal, root )
								setParent( par, root )
								insertInternal( root, 0, middle, newinternal )
								par = root
							} else {
								par = getParent( par )
								binarySearch( par, middle ) match {
									case index if index >= 0 => sys.error( "key found in internal node" )
									case insertion => insertInternal( par, -(insertion + 1), middle, newinternal )
								}
							}
						}
				}
			}
		}
	}
	
	/**
	 * Performs the B+ Tree deletion algorithm to remove `key` and it's associated value from the tree, rebalancing the tree if necessary.
	 * 
	 * @return `true` if `key` was found (and therefore removed), `false` otherwise
	 */
	def delete( key: K ) = {
		lookup( key ) match {
			case (true, leaf, index) =>
				val key = getKey( leaf, index )
				val len = removeLeaf( leaf, index )
				
				if (len < order/2) {
					var par = getParent( leaf )
					val (sibling, leafside, siblingside, left, right, parkey) = {
						val next = getNext( leaf )
						
						if (next != nul && getParent( next ) == par) {
							(next, len, 0, leaf, next, if (len == 0) key else getKey( leaf, nodeLength(leaf) - 1 ))
						} else {
							val prev = getPrev( leaf )
							
							if (prev != nul && getParent( prev ) == par)
								(prev, 0, nodeLength( prev ) - 1, prev, leaf, getKey( prev, nodeLength(prev) - 1 ))
							else
								sys.error( "no sibling" )
						}
					}
					
					val index =
						binarySearch( par, parkey ) match {
							case ind if ind >= 0 => ind
							case ind => -(ind + 1)
						}
					
					if (nodeLength( sibling ) > order/2) {
						moveLeaf( sibling, siblingside, siblingside + 1, leaf, leafside )
						setKey( par, index, getKey(right, 0) )
					} else {
						moveLeaf( right, 0, nodeLength(right), left, nodeLength(left) )
						
						val next = getNext( right )
						
						setNext( left, next )
						
						if (next == nul) {
							last = left
							setLast( left )
							lastlen = nodeLength( left )
						}
					
						freeNode( right )
						
						var len = removeInternal( par, index )
							
						if (par == root && len == 0) {
							freeNode( root )
							setParent( left, nul )
							root = left
							setRoot( left )
							first = left
							setFirst( left )
						} else if (par != root)
							while (len < order/2) {
								
							}
					}
				}
				
				true
			case (false, leaf, index) => false
		}
	}
	
	/**
	 * Analyzes the tree to determine if it is well constructed.
	 * 
	 * @return `"true"` (as a string) if the tree is a well constructed B+ Tree, a string description of the flaw otherwise.
	 */
	def wellConstructed: String = {
		val nodes = new ArrayBuffer[N]
		var depth = -1
		var prevnode: N = nul
		var nextptr: N = nul
		val cbo2 = order/2 + order%2
		
		def check( n: N, p: N, d: Int ): String = {
			if (!(getKeys( n ).dropRight( 1 ) zip getKeys( n ).drop( 1 ) forall( p => p._1 < p._2 )))
				return "incorrectly ordered keys"
			
			if (getParent( n ) != p)
				return "incorrect parent pointer in level " + d
				
			if (isLeaf( n )) {
				if (depth == -1)
					depth = d
				else if (d != depth)
					return "leaf nodes not at same depth"
				
				if (getParent( n ) == nul) {
					if (nodeLength( n ) >= order)
						return "root leaf node length out of range"
				} else if (nodeLength( n ) < cbo2 - 1 || nodeLength( n ) > order - 1)
					return "non-root leaf node length out of range"
					
				if (prevnode == nul && first != n)
					return "incorrect first pointer"
					
				if (prevnode != getPrev( n ))
					return "incorrect prev pointer"
				else
					prevnode = n
					
				if (getNext( n ) == nul && last != n)
					return "incorrect last pointer"
					
				if ((nextptr != nul) && (nextptr != n))
					return "incorrect next pointer"
				else
					nextptr = getNext( n )
			}
			else {
				if (getBranches( n ) exists (p => p == nul))
					return "null branch pointer"
					
				if (getKeys( getBranch(n, 0) ) isEmpty)
					return "empty internal node"
					
				if (getKeys( n ) isEmpty)
					return "empty internal node"
				
				if (getParent( n ) == nul) {
					if (nodeLength( n ) < 1 || nodeLength( n ) >= order)
						return "root internal node length out of range"
				} else if (nodeLength( n ) < cbo2 - 1 || nodeLength( n ) > order - 1)
					return "non-root internal node length out of range"
					
				if (getKeys( getBranch(n, 0) ).last >= getKey( n, 0 ))
					return "left internal node branch not strictly less than"
					
				if (!(getKeys( n ) drop 1 zip getBranches( n ) drop 1 forall (p => getKey( p._2, 0 ) < p._1)))
					return "right internal node branch not greater than or equal"
				
				for (b <- getBranches( n ))
					check( b, n, d + 1 ) match {
						case "true" =>
						case error => return error
					}
			}
			
			"true"
		}
		
		check( root, nul, 0 ) match {
			case "true" =>
			case error => return error
		}
			
		if (nextptr != nul)
			return "rightmost next pointer not null"
			
		"true"
	}
	
	/**
	 * Returns a string representing a search result for `key` that will be consistant with `prettyPrint`. This method is used mainly for unit testing.
	 */
	def prettySearch( key: K ) = {
		val map = new HashMap[N, String]
		var count = 0
		
		def traverse( nodes: List[N] ) {
			for (n <- nodes) {
				map(n) = "n" + count
				count += 1
			}
			
			if (!isLeaf( nodes.head ))
				traverse( nodes flatMap getBranches )
		}
		
		traverse( List(root) )
		
		lookup( key ) match {
			case (true, leaf, index) => map(leaf) + " " + getValue( leaf, index ) + " " + index
			case _ => "not found"
		}
	}
	
	/**
	 * Returns a serialization (string representation of the tree) using string and function arguments to specify the exact form of the serialization. This method is used for ''pretty printing'' and to generate a DOT (graph description language) description of the tree so that it can be visualized.
	 * 
	 * @param before string to be prepended to the serialization
	 * @param prefix string to be place before each line of the serialization that includes internal and leaf nodes
	 * @param internalnode function to generate internal node serializations using three parameters: the current node, a function to return a string id of a node, a function that allows a line of text to be appended after all nodes have been serialized
	 * @param leafnode function to generate leaf node serializations using two parameters: the current node, a function to return a string id of a node
	 * @param after string to be appended to the serialization
	 */
	protected def serialize( before: String, prefix: String, internalnode: (N, N => String, String => Unit) => String, leafnode: (N, N => String) => String, after: String ) = {
		val buf = new StringBuilder( before )
		val afterbuf = new StringBuilder
		val map = new HashMap[N, String]
		var count = 0
		
		def id( node: N ) =
			if (node == nul)
				"null"
			else
				map get node match {
					case Some( n ) => n
					case None =>
						val n = "n" + count
						map(node) = n
						count += 1
						n
				}
		
		def emit( s: String ) = {
			afterbuf ++= prefix
			afterbuf ++= s
			afterbuf += '\n'
		}
		
		def printNodes( nodes: List[N] ) {
			if (isLeaf( nodes.head )) {
				buf ++= prefix
				buf ++= nodes map (n => leafnode(n, id)) mkString " "
			} else {
				buf ++= prefix
				buf ++= nodes map (n => internalnode(n, id, emit)) mkString " "
				buf += '\n'
				printNodes( nodes flatMap getBranches )
			}
		}

		printNodes( List(root) )
		
		if (!afterbuf.isEmpty || after != "")
			buf += '\n'
			
		buf ++= afterbuf.toString
		buf ++= after
		buf toString
	}
	
	/**
	 * Prints (to stdout) a readable representation of the structure and contents of the tree.
	 */
	def prettyPrint = println( prettyStringWithValues )
	
	/**
	 * Prints (to stdout) a readable representation of the structure and contents of the tree, omitting the values and only printing the keys.
	 */
	def prettyPrintKeysOnly = println( prettyString )
	
	/**
	 * Returns a string containing a readable representation of the structure and contents of the tree, omitting the values and only printing the keys. This method is used mainly for unit testing.
	 */
	def prettyString = serialize( "", "", (n, id, _) => "[" + id(n) + ": (" + id(getParent(n)) + ") " + id(getBranch(n, 0)) + " " + getKeys(n).zipWithIndex.map({case (k, j) => "| " + k + " | " + id(getBranch(n, j + 1))}).mkString(" ") + "]", (n, id) => "[" + id(n) + ": (" + id(getPrev(n)) + ", " + id(getParent(n)) + ", " + id(getNext(n)) + ")" + (if (nodeLength(n) == 0) "" else " ") + getKeys(n).mkString(" ") + "]", "" )
	
	/**
	 * Returns a string containing a readable representation of the structure and contents of the tree. This method is used mainly for unit testing.
	 */
	def prettyStringWithValues = serialize( "", "", (n, id, _) => "[" + id(n) + ": (" + id(getParent(n)) + ") " + id(getBranch(n, 0)) + " " + getKeys(n).zipWithIndex.map({case (k, j) => "| " + k + " | " + id(getBranch(n, j + 1))}).mkString(" ") + "]", (n, id) => "[" + id(n) + ": (" + id(getPrev(n)) + ", " + id(getParent(n)) + ", " + id(getNext(n)) + ")" + (if (nodeLength(n) == 0) "" else " ") + (getKeys(n) zip getValues(n) map (p => "<" + p._1 + ", " + p._2 + ">") mkString " ") + "]", "" )
	
	/**
	 * Creates a PNG image file called `name` (with `.png` added) which visually represents the structure and contents of the tree, only showing the keys. This method uses GraphViz (specifically the `dot` command) to produce the diagram, and ImageMagik (specifically the `convert` command) to convert it from SVG to PNG. `dot` can product PNG files directly but I got better results producing SVG and converting to PNG.
	 */
	def diagram( name: String ) {
		val before =
			"""	|digraph {
					|    graph [splines=line];
					|    edge [penwidth=2];
					|    node [shape = record, height=.1, width=.1, penwidth=2, style=filled, fillcolor=white];
					|
					|""".stripMargin

		def internalnode( n: N, id: N => String, emit: String => Unit ) = {
			val buf = new StringBuilder( id(n) + """[label = "<b0> &bull;""" )
			
			emit( id(n) + ":b0" + " -> " + id(getBranch(n, 0)) + ";" )
			
			for ((k, i) <- getKeys(n) zipWithIndex) {
				buf ++= " | " + k + " | <b" + (i + 1) + "> &bull;"
				emit( id(n) + ":b" + (i + 1) + " -> " + id(getBranch(n, i + 1)) + ";" )
			}
			
			buf ++= """"];"""
			buf toString
		}
		
// 		def leafnode( n: N, id: N => String ) = id(n) + """[label = "<prev> &bull; | """ + (getKeys(n) mkString " | ") + """ | <next> &bull;"];"""
		def leafnode( n: N, id: N => String ) = id(n) + """[label = """" + (getKeys(n) mkString " | ") + """"];"""
		
		val file = new PrintWriter( name + ".dot" )
		
		file.println( serialize(before, "    ", internalnode, leafnode, "}") )
		file.close
		s"dot -Tsvg $name.dot -o $name.svg".!
		s"convert $name.svg $name.png".!
	}
}