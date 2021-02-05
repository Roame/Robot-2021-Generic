package frc.taurus.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.flatbuffers.FlatBufferBuilder;

import org.junit.Test;

import frc.taurus.messages.generated.TestMessage1;

public class MessageQueueTest {
    
    // verify queue can be reset/cleared
    @Test
    public void resetTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 686);
        builder.finish(offset);
        queue.write(builder.dataBuffer());     
        
        assertEquals(1, reader.size());         // verify queue has 1 element
        assertFalse(reader.isEmpty());
        
        queue.clear();                          // clear queue
        
        assertEquals(0, reader.size());         // verify queue is empty
        assertTrue(reader.isEmpty());
    }



    // verify we can send one value
    @Test
    public void sendSingleValueTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 686);
        builder.finish(offset);
        queue.write(builder.dataBuffer());     

        
        assertEquals(1, reader.size());         // verify queue has 1 element
        Optional<ByteBuffer> opt = reader.read();  // read from queue
        assertTrue(opt.isPresent());            // verify that the value is present (not empty)
        
        TestMessage1 msg = TestMessage1.getRootAsTestMessage1( opt.get() );            // get the element
        assertEquals(686, msg.intValue());      // verify contents are 686

        assertEquals(0, reader.size());         // verify queue is now empty
        assertTrue(reader.isEmpty());           // verify queue is now empty

    }
    

    // verify we can send one value
    @Test (expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void forgotToFinishFlatBuffer() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 686);
        
        // this is the line that we forgot
        // builder.finish(offset);

        // builder.dataBuffer() should throw an AssertionError
        queue.write(builder.dataBuffer());     
    }
    

    // verify GenericQueue.readLast() returns the last element, always
    @Test
    public void readLastTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();

        // you must create a new builder for each write, 
        // or all written values will end up being the same        
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 254);
        builder.finish(offset);
        queue.write(builder.dataBuffer());     
       

        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 971);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
        

        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 686);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
  
    
        // the last element should always be 686 when using the queue's readLast()
        // even if queried several times
        for (int k=1; k<=5; k++)
        {
            Optional<ByteBuffer> opt = reader.readLast();    // read the last element
            assertTrue(opt.isPresent());                 // verify that the value is present (not empty)

            TestMessage1 msg = TestMessage1.getRootAsTestMessage1( opt.get() );                   // get the value
            assertEquals(686, msg.intValue());           // verify contents are 686
        }
    }

    // check that queue reader reads several values correctly
    @Test
    public void readerReadTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();

        // you must create a new builder for each write, 
        // or all written values will end up being the same
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 254);
        builder.finish(offset);
        queue.write(builder.dataBuffer());     
   

        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 971);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
        
        
        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 686);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
  
        
        assertEquals(3, reader.size());
        Optional<ByteBuffer> opt = reader.read();   // read the next value, removing it
        assertTrue(opt.isPresent());             // verify that the value is present (not empty)
        TestMessage1 msg = TestMessage1.getRootAsTestMessage1( opt.get() );               // get the value
        assertEquals(254, msg.intValue());       // verify contents are 254
        
        assertEquals(2, reader.size());
        opt = reader.read();                     // read the next value    
        assertTrue(opt.isPresent());                                
        msg = TestMessage1.getRootAsTestMessage1( opt.get() );                                      
        assertEquals(971, msg.intValue());       // verify contents are 971                    
        
        assertEquals(1, reader.size());
        opt = reader.read();                     // read the next value    
        assertTrue(opt.isPresent());                                
        msg = TestMessage1.getRootAsTestMessage1( opt.get() );                              
        assertEquals(686, msg.intValue());       // verify contents are 686                     
        
        assertEquals(0, reader.size());
        assertTrue(reader.isEmpty());           // verify all elements have been read out
        opt = reader.read();                    // attempt to read one more element
        assertTrue(opt.isEmpty());              // check that it is empty
        assertFalse(opt.isPresent());           // check that it is empty
    }  
    
    
    
    // check that queue reader reads the last value correctly
    @Test
    public void readerReadLastTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        // you must create a new builder for each write, 
        // or all written values will end up being the same
        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 254);
        builder.finish(offset);
        queue.write(builder.dataBuffer());     
       
 
        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 971);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
        
 
        builder = new FlatBufferBuilder(64);
        offset = TestMessage1.createTestMessage1(builder, 686);
        builder.finish(offset);
        queue.write(builder.dataBuffer());      
  
    
        assertEquals(3, reader.size());
        assertFalse(reader.isEmpty());

        Optional<ByteBuffer> opt = reader.readLast();  // read the last value, remove all values
        assertTrue(opt.isPresent());                // verify that the value is present (not empty)
        TestMessage1 msg = TestMessage1.getRootAsTestMessage1( opt.get() );                  // get the value
        assertEquals(686, msg.intValue());          // verify contents are 686

        assertEquals(0, reader.size());
        assertTrue(reader.isEmpty());

        opt = reader.read();                        // read the last value
        assertTrue(opt.isEmpty());                  // element should be empty
    }    



    // check that queue delivers multiple values correctly, and in sequence
    @Test
    public void deliverManyValuesTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        for (int k=0; k<10; k++) {
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
       
        }                            

        int cnt = 0;            
        Optional<ByteBuffer> opt = reader.read();
        while (opt.isPresent()) {
            assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());  // check we are reading counting pattern
            opt = reader.read();
        }

        assertEquals(10, cnt);  // check that all 10 values were read out
    }   
    
    
    // ensure that correctness is maintained even when the circular buffer wraps
    // and we start to overwrite the beginning of the buffer
    @Test
    public void wraparoundTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        queue.clear();

        // fill queue with counting pattern
        for (int k=0; k<10; k++) {
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
        }

        int cnt = 0;
        Optional<ByteBuffer> opt = reader.read();
        while (opt.isPresent()) {
            assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
            opt = reader.read();            
        }

        // continue filling queue with counting pattern
        for (int k=10; k<15; k++) {
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
        }

        opt = reader.read();
        while (opt.isPresent()) {
            assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
            opt = reader.read();            
        }

        assertTrue(reader.isEmpty());
    }


    // ensure that the queue works with multiple readers
    @Test
    public void multipleReaderTest() {
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        
        var reader1 = queue.makeReader();
        var reader2 = queue.makeReader();
        var reader3 = queue.makeReader();
        var reader4 = queue.makeReader();

        // fill queue with counting pattern
        for (int k=0; k<9; k++) {
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
        }

        assertFalse(reader1.isEmpty());
        int cnt = 0;
        var opt = reader1.read();
        assertTrue(opt.isPresent());
        while (opt.isPresent()) {
            assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
            opt = reader1.read();            
        }
        assertTrue(reader1.isEmpty());
        
        assertFalse(reader2.isEmpty());
        cnt = 0;
        opt = reader2.read();
        assertTrue(opt.isPresent());
        while (opt.isPresent()) {
            assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
            opt = reader2.read();            
        }   
        assertTrue(reader2.isEmpty());
        
        assertFalse(reader3.isEmpty());
        assertFalse(reader4.isEmpty());
        for (int k=0; k<9; k++) {
            opt = reader3.read();
            assertTrue(opt.isPresent());
            assertEquals(k, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
            
            opt = reader4.read();
            assertTrue(opt.isPresent());
            assertEquals(k, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
        }
        assertTrue(reader3.isEmpty());
        assertTrue(reader4.isEmpty());
    }     


    // Speed test for profiling
    @Test
    public void speedTest() {
        final int numValues = 1000000;
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(numValues){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
        
        // fill queue with counting pattern
        for (int k=0; k<numValues; k++) {
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
        }

        int cnt = 0;
        while (!reader.isEmpty()) {
            var opt = reader.read();
            if (opt.isPresent()) {
                assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
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
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(kNumValues){};

        // start reader threads
        Thread[] threads = new Thread[numThreads];
        for (int k=0; k<threads.length; k++) {
            threads[k] = new Thread(new Runnable() {
                public void run() {
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " started");
                    MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
                    int cnt = 0;
                    long start = System.currentTimeMillis();
                    long end = start + timeout;
                    while (cnt < kNumValues && System.currentTimeMillis() < end)
                    {
                        while (!reader.isEmpty()) {
                            var opt = reader.read();
                            if (opt.isPresent()) {
                                assertEquals(cnt++, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());
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
            FlatBufferBuilder builder = new FlatBufferBuilder(64);
            int offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
        }
        // System.out.println("Wrote " + kNumValues + " elements");   

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
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(kNumValuesPerThread*kNumThreads){};

        CountDownLatch latch = new CountDownLatch(kNumThreads);
        AtomicReference<AssertionError> failure = new AtomicReference<>();

        // start reader threads
        Thread[] readerThreads = new Thread[kNumThreads];
        for (int k=0; k<readerThreads.length; k++) {
            readerThreads[k] = new Thread(new Runnable() {
                public void run() {
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " started");
                    MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
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
                        FlatBufferBuilder builder = new FlatBufferBuilder(64);
                        int offset = TestMessage1.createTestMessage1(builder, k);
                        builder.finish(offset);
                        ByteBuffer bb = builder.dataBuffer();
                        queue.write(bb);      
                    }
                    // System.out.println(Thread.currentThread().getName() 
                    //          + " wrote " + kNumValuesPerThread + " elements"); 
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
        assertNull(failure.get());
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
        MessageQueue<ByteBuffer> queue = new MessageQueue<ByteBuffer>(10){};
        MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();

        assertEquals(0, reader.nextReadIndex);

        FlatBufferBuilder builder = new FlatBufferBuilder(64);
        int offset = TestMessage1.createTestMessage1(builder, 686);
                builder.finish(offset);
        queue.write(builder.dataBuffer());     
 
        assertEquals(0, reader.nextReadIndex);
        
        var opt = reader.read();
        assertEquals(1, reader.nextReadIndex);

        // write 11 numbers
        for (int k=0; k<11; k++) {
            builder = new FlatBufferBuilder(64);
            offset = TestMessage1.createTestMessage1(builder, k);
            builder.finish(offset);
            ByteBuffer bb = builder.dataBuffer();
            queue.write(bb);      
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
        assertEquals(1, TestMessage1.getRootAsTestMessage1( opt.get() ).intValue());         
        assertEquals(3, reader.nextReadIndex);  
    }
}