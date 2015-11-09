/* This file is part of Restructured, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.Restructured.chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple logical lock manager using tokens.  Used by
 * MyThreadedFileIOBase to get a logical lock on a chunk
 * in order to manage concurrent updates by multiple
 * threads.
 */
public class LockManager<T> {
	
	// pool of names that are being locked
	private final Set<T> pool = new HashSet<T>(); 
	
	public LockManager() {
		
	}

	public void lock(final T token) throws InterruptedException {
	    synchronized(pool) {
	        while(pool.contains(token)) {
	        	System.out.println("LOCK MANAGER BLOCK");
	            pool.wait();
	        }
	        pool.add(token);
	    }
	}

	public void unlock(final T token) {
	    synchronized(pool) {
	        pool.remove(token);
	        pool.notifyAll();
	    }
	}
}
