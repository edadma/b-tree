package xyz.hyperreal


package object btree {
	
	def binarySearch[E, K <% Ordered[K]]( seq: IndexedSeq[E], target: K, comparator: (E, K) => Int ): Int = {
		def search( start: Int, end: Int ): Int = {
			if (start > end)
				-start - 1
			else {
				val mid = start + (end-start + 1)/2
				
				if (comparator( seq(mid), target ) == 0)
					mid
				else if (comparator( seq(mid), target ) > 0)
					search( start, mid - 1 )
				else
					search( mid + 1, end )
			}
		}
		
		search( 0, seq.length - 1 )
	}

}