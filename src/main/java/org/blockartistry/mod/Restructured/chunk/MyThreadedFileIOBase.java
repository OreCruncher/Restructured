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

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.world.storage.IThreadedFileIO;

/**
 * Replacement ThreadedFileIOBase. It improves on the Vanilla version by:
 * 
 * - Uses a LinkedBlockingDeque for queuing tasks to be executed.
 * 
 * - Capable of having multiple threads servicing the queue.
 * 
 * - Efficient wait on the queue for work. No sleeps or other timers. Immediate
 * dispatch of work when it is queued.
 * 
 * - Simple AtomicInteger for tracking work that is being performed.
 *
 * - Doesn't have the strange behavior of leaving a task in the queue when it is
 * being executed. Tasks are removed and it is up to application logic to put
 * the appropriate items into the queue for servicing. (This works with the
 * AnvilChunkLoader code.)
 * 
 */
public class MyThreadedFileIOBase implements Runnable {

	private final static int THREAD_PRIORITY = Thread.NORM_PRIORITY;
	private final static int THREAD_COUNT = 3;
	private final static String THREAD_GROUP_NAME = "File IO";

	private final static LinkedBlockingDeque<IThreadedFileIO> workQueue = new LinkedBlockingDeque<IThreadedFileIO>();
	private final static ThreadGroup workers = new ThreadGroup(THREAD_GROUP_NAME);
	private final static AtomicInteger outstandingWork = new AtomicInteger();

	public final static MyThreadedFileIOBase threadedIOInstance = new MyThreadedFileIOBase(THREAD_COUNT);

	private MyThreadedFileIOBase() {
		this(1);
	}

	private MyThreadedFileIOBase(final int threadCount) {
		workers.setDaemon(true);
		for (int i = 0; i < threadCount; i++) {
			final String threadName = new StringBuilder().append(THREAD_GROUP_NAME).append(" #").append(i + 1)
					.toString();
			final Thread thread = new Thread(workers, this, threadName);
			thread.setPriority(THREAD_PRIORITY);
			thread.start();
		}
	}

	public void run() {
		do {

			try {

				// Service an item from the queue. The outstanding work
				// is decremented AFTER the service.
				final IThreadedFileIO task = workQueue.take();
				task.writeNextIO();
				outstandingWork.decrementAndGet();

			} catch (final InterruptedException ie) {
				;
			}

		} while (true);
	}

	public void queueIO(final IThreadedFileIO task) {
		// Queue up a valid task. The outstanding work counter
		// is incremented BEFORE it is queued.
		if (task != null) {
			outstandingWork.incrementAndGet();
			workQueue.add(task);
		}
	}

	public void waitForFinish() throws InterruptedException {
		// Wait for the work to drain from the queue
		while (outstandingWork.get() != 0)
			Thread.sleep(10L);
	}
}
