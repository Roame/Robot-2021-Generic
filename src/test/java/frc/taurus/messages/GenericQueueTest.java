package frc.taurus.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class GenericQueueTest {
    
    // verify queue can be reset/cleared
    @Test
    public void resetTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();

        queue.write(686);                       // write value to queue
        
        assertEquals(1, reader.size());         // verify queue has 1 element
        assertFalse(reader.isEmpty());
        
        queue.clear();                          // clear queue
        
        assertEquals(0, reader.size());         // verify queue is empty
        assertTrue(reader.isEmpty());
    }


    // verify we can send one value
    @Test
    public void sendSingleValueTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();

        queue.write(686);                       // write value to queue
        
        assertEquals(1, reader.size());         // verify queue has 1 element
        Optional<Integer> opt = reader.read();  // read from queue
        assertTrue(opt.isPresent());            // verify that the value is present (not empty)
        
        Integer value = opt.get();              // get the element
        assertEquals((Integer)686, value);      // verify contents are 686

        assertEquals(0, reader.size());         // verify queue is now empty
        assertTrue(reader.isEmpty());           // verify queue is now empty
    }
    
    // verify GenericQueue.readLast() returns the last element, always
    @Test
    public void queueReadLastTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        
        queue.write(254);                            // write 3 elements into queue
        queue.write(971);                            
        queue.write(686);                           
    
        // the last element should always be 686 when using the queue's readLast()
        // even if queried several times
        for (int k=1; k<=5; k++)
        {
            Optional<Integer> opt = queue.readLast();    // read the last element
            assertTrue(opt.isPresent());                 // verify that the value is present (not empty)

            Integer value = opt.get();                   // get the value
            assertEquals((Integer)686, value);           // verify contents are 686
        }
    }

    // check that queue reader reads several values correctly
    @Test
    public void readerReadTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        
        queue.write(254);                       // write 3 elements into queue     
        queue.write(971);                            
        queue.write(686);                            
    
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();

        assertEquals(3, reader.size());
        Optional<Integer> opt = reader.read();   // read the next value, removing it
        assertTrue(opt.isPresent());             // verify that the value is present (not empty)
        Integer value = opt.get();               // get the value
        assertEquals((Integer)254, value);       // verify contents are 254

        assertEquals(2, reader.size());
        opt = reader.read();                     // read the next value    
        assertTrue(opt.isPresent());                                
        value = opt.get();                                      
        assertEquals((Integer)971, value);       // verify contents are 971                    

        assertEquals(1, reader.size());
        opt = reader.read();                     // read the next value    
        assertTrue(opt.isPresent());                                
        value = opt.get();                              
        assertEquals((Integer)686, value);       // verify contents are 686                     

        assertEquals(0, reader.size());
        assertTrue(reader.isEmpty());           // verify all elements have been read out
        opt = reader.read();                    // attempt to read one more element
        assertTrue(opt.isEmpty());              // check that it is empty
        assertFalse(opt.isPresent());           // check that it is empty
    }  



    // check that queue reader reads the last value correctly
    @Test
    public void readerReadLastTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        
        queue.write(254);                           // write three values into queue               
        queue.write(971);                            
        queue.write(686);                            
    
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();

        assertEquals(3, reader.size());
        assertFalse(reader.isEmpty());

        Optional<Integer> opt = reader.readLast();  // read the last value, remove all values
        assertTrue(opt.isPresent());                // verify that the value is present (not empty)
        Integer value = opt.get();                  // get the value
        assertEquals((Integer)686, value);          // verify contents are 686

        assertEquals(0, reader.size());
        assertTrue(reader.isEmpty());

        opt = reader.read();                        // read the last value
        assertTrue(opt.isEmpty());                  // element should be empty
    }    



    // check that queue delivers multiple values correctly, and in sequence
    @Test
    public void deliverManyValuesTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();    
        
        for (int k=0; k<10; k++) {
            queue.write(k);      // write a counting pattern
        }                            

        int cnt = 0;            
        Optional<Integer> opt = reader.read();
        while (opt.isPresent()) {
            assertEquals((Integer)cnt++, opt.get());  // check we are reading counting pattern
            opt = reader.read();
        }

        assertEquals(10, cnt);  // check that all 10 values were read out
    }   
    
    
    // ensure that correctness is maintained even when the circular buffer wraps
    // and we start to overwrite the beginning of the buffer
    @Test
    public void wraparoundTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();    
        
        queue.clear();

        // fill queue with counting pattern
        for (int k=0; k<10; k++) {
            queue.write(k);
        }

        int cnt = 0;
        Optional<Integer> opt = reader.read();
        while (opt.isPresent()) {
            assertEquals((Integer)cnt++, opt.get());
            opt = reader.read();            
        }

        // continue filling queue with counting pattern
        for (int k=10; k<15; k++) {
            queue.write(k);
        }

        opt = reader.read();
        while (opt.isPresent()) {
            assertEquals((Integer)cnt++, opt.get());
            opt = reader.read();            
        }

        assertTrue(reader.isEmpty());
    }


    // ensure that the queue works with multiple readers
    @Test
    public void multipleReaderTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        
        var reader1 = queue.makeReader();
        var reader2 = queue.makeReader();
        var reader3 = queue.makeReader();
        var reader4 = queue.makeReader();

        // fill queue with counting pattern
        for (int k=0; k<9; k++) {
            queue.write(k);
        }

        assertFalse(reader1.isEmpty());
        int cnt = 0;
        var opt = reader1.read();
        assertTrue(opt.isPresent());
        while (opt.isPresent()) {
            assertEquals((Integer)cnt++, opt.get());
            opt = reader1.read();            
        }
        assertTrue(reader1.isEmpty());
        
        assertFalse(reader2.isEmpty());
        cnt = 0;
        opt = reader2.read();
        assertTrue(opt.isPresent());
        while (opt.isPresent()) {
            assertEquals((Integer)cnt++, opt.get());
            opt = reader2.read();            
        }   
        assertTrue(reader2.isEmpty());
        
        assertFalse(reader3.isEmpty());
        assertFalse(reader4.isEmpty());
        for (int k=0; k<9; k++) {
            opt = reader3.read();
            assertTrue(opt.isPresent());
            assertEquals((Integer)k, opt.get());
            
            opt = reader4.read();
            assertTrue(opt.isPresent());
            assertEquals((Integer)k, opt.get());
        }
        assertTrue(reader3.isEmpty());
        assertTrue(reader4.isEmpty());
    }     


    // Speed test for profiling
    @Test
    public void speedTest() {
        final int numValues = 1000000;
        MessageQueue<Integer> queue = new MessageQueue<Integer>(numValues);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();    
        
        // fill queue with counting pattern
        for (int k=0; k<numValues; k++) {
            queue.write(k);
        }

        int cnt = 0;
        while (!reader.isEmpty()) {
            var opt = reader.read();
            if (opt.isPresent()) {
                assertEquals((Integer)cnt++, opt.get());
            }
        }
        assertEquals(1000000, cnt);        
    }



    // ensure that queues maintain correctness with a single writer thread 
    // and several reader threads
    @Test
    public void multipleReaderThreadTest() {
        final int kNumValues = 10000;
        final int numThreads = 5;
        final long timeout = 1000;  // milliseconds
        MessageQueue<Integer> queue = new MessageQueue<Integer>(kNumValues);

        // start reader threads
        Thread[] threads = new Thread[numThreads];
        for (int k=0; k<threads.length; k++) {
            threads[k] = new Thread(new Runnable() {
                public void run() {
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " started");
                    MessageQueue<Integer>.QueueReader reader = queue.makeReader();                    
                    int cnt = 0;
                    long start = System.currentTimeMillis();
                    long end = start + timeout;
                    while (cnt < kNumValues && System.currentTimeMillis() < end)
                    {
                        while (!reader.isEmpty()) {
                            var opt = reader.read();
                            if (opt.isPresent()) {
                                assertEquals((Integer)cnt++, opt.get());
                            }
                        }
                    }
                    assertEquals(kNumValues, cnt);  
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " read " + cnt + " elements");                     
                }             
            });
            threads[k].start();
        }

        // start writing
        for (int k=0; k<kNumValues; k++) {
            queue.write(k);
        }

        // wait for all threads to complete before stopping this test function
        for (int k=0; k<threads.length; k++) {
            try {
                threads[k].join(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    // ensure that all the data written is read by readers
    // with multiple writer threads and multiple reader threads

    // testing multiple threads using technique documented here:
    // (the first method, not the ConcurrentUnit Waiter method)
    // https://jodah.net/testing-multi-threaded-code

    @Test
    public void multipleWriterReaderThreadTest() {
        final int kNumValuesPerThread = 10000;
        final int kNumThreads = 5;
        final long timeout = 1000;  // milliseconds
        MessageQueue<Integer> queue = new MessageQueue<Integer>(kNumValuesPerThread * kNumThreads);

        CountDownLatch latch = new CountDownLatch(kNumThreads);
        AtomicReference<AssertionError> failure = new AtomicReference<>();

        // start reader threads
        Thread[] readerThreads = new Thread[kNumThreads];
        for (int k=0; k<readerThreads.length; k++) {
            readerThreads[k] = new Thread(new Runnable() {
                public void run() {
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " started");
                    MessageQueue<Integer>.QueueReader reader = queue.makeReader();                    
                    int cnt = 0;
                    long start = System.currentTimeMillis();
                    long end = start + timeout;
                    while (cnt < kNumValuesPerThread * kNumThreads && System.currentTimeMillis() < end)
                    {
                        while (!reader.isEmpty()) {
                            var opt = reader.read();
                            if (opt.isPresent()) {
                                // not looking at contents, which will be jumbled
                                cnt++;
                            }
                        }
                    }
                    try {
                        assertEquals(kNumValuesPerThread * kNumThreads, cnt);
                    } catch (AssertionError e) {
                        failure.set(e);
                    }
                    latch.countDown();  // each time a thread succeeds, count down one
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " read " + cnt + " elements");                     
                }             
            });
        }

        // start writer threads
        Thread[] writerThreads = new Thread[kNumThreads];
        for (int k=0; k<writerThreads.length; k++) {
            writerThreads[k] = new Thread(new Runnable() {
                public void run() {
                    for (int k=0; k<kNumValuesPerThread; k++) {
                        queue.write(k);
                    }
                }             
            });
        }

        // start all reader threads
        for (int k=0; k<readerThreads.length; k++) {
            readerThreads[k].start();
        }

        // start all writer threads
        for (int k=0; k<writerThreads.length; k++) {
            writerThreads[k].start();
        }

        // wait to see if all reader threads complete successfully within timeout period
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // throw any failures found in reader threads
        if (failure.get() != null)
            throw failure.get();


        // wait for all threads to complete before stopping this test function
        for (int k=0; k<readerThreads.length; k++) {
            try {
                readerThreads[k].join(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int k=0; k<writerThreads.length; k++) {
            try {
                writerThreads[k].join(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }        
    } 
    
    
    // check that reader index moves up when reader gets too far behind
    @Test
    public void readerIndexTest() {
        MessageQueue<Integer> queue = new MessageQueue<Integer>(10);
        MessageQueue<Integer>.QueueReader reader = queue.makeReader();    

        assertEquals(0, reader.nextReadIndex);

        queue.write(686);
        assertEquals(0, reader.nextReadIndex);
        
        var opt = reader.read();
        assertEquals(1, reader.nextReadIndex);

        // write 11 numbers
        for (int k=0; k<11; k++) {
            queue.write(k);
        }

        // because the queue only uses a buffer with length 10, the
        // following numbers were overwritten:
        // 686-->9
        //   0-->10
        // the oldest number in the buffer (that wasn't overwritten)
        // is now the number 1 at index 2

        // verify that we skip the number 0 (at index 1)
        // verify we read the number 1 (at index 2)
        // verify the nextReadIndex is 3
        assertEquals(2, queue.front());
        assertEquals(12, queue.back());
        opt = reader.read();
        assertTrue(opt.isPresent());
        assertEquals((Integer)1, opt.get());         
        assertEquals(3, reader.nextReadIndex);  
        }
}