package com.ibm.streamsx.topology.test.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.function7.Consumer;
import com.ibm.streamsx.topology.function7.Function;
import com.ibm.streamsx.topology.function7.Predicate;
import com.ibm.streamsx.topology.function7.Supplier;
import com.ibm.streamsx.topology.function7.UnaryOperator;
import com.ibm.streamsx.topology.test.TestTopology;

public class AutoClosableTest extends TestTopology {
    
    @Before
    public void onlyInEmbedded() {
        assumeTrue(isMainRun());
    }
    
    /**
     * Test close is called for various transformations.
     * @throws Exception
     */
    @Test
    public void testCloseIsCalled() throws Exception {
        Topology topology = new Topology();
        
        TStream<String> stream = topology.source(new CloseSupplier(), String.class);
        stream = stream.filter(new ClosePredicate()) ;
        stream = stream.modify(new CloseUnary()) ;
        stream = stream.multiTransform(new CloseMultiTransform(), String.class) ;
        stream.sink(new CloseConsumer());
        
        this.getTesterContext().submit(topology).get();
        
        assertTrue(CloseSupplier.seenClose.get());
        assertTrue(ClosePredicate.seenClose.get());
        assertTrue(CloseUnary.seenClose.get());
        assertTrue(CloseMultiTransform.seenClose.get());
        assertTrue(CloseConsumer.seenClose.get());       
    }
    
    public static class CloseSupplier implements Supplier<Iterable<String>>, AutoCloseable {
        private static final long serialVersionUID = 1L;
        public final static AtomicBoolean seenClose = new AtomicBoolean();

        @Override
        public void close() {
            seenClose.set(true);          
        }

        @Override
        public Iterable<String> get() {
            return Collections.singleton("42!");
        }
    }
    public static class CloseConsumer implements Consumer<String>, AutoCloseable {
        private static final long serialVersionUID = 1L;
        public final static AtomicBoolean seenClose = new AtomicBoolean();

        @Override
        public void close() {
            seenClose.set(true);          
        }

        @Override
        public void accept(String v) {
        }      
    }
    public static class ClosePredicate implements Predicate<String>, AutoCloseable {
        private static final long serialVersionUID = 1L;
        public final static AtomicBoolean seenClose = new AtomicBoolean();

        @Override
        public void close() {
            seenClose.set(true);          
        }

        @Override
        public boolean test(String v) {
            return true;
        }      
    }
    public static class CloseUnary implements UnaryOperator<String>, AutoCloseable {
        private static final long serialVersionUID = 1L;
        public final static AtomicBoolean seenClose = new AtomicBoolean();

        @Override
        public void close() {
            seenClose.set(true);          
        }

        @Override
        public String apply(String v) {
            return v;
        }      
    }
    public static class CloseMultiTransform implements Function<String,Iterable<String>>, AutoCloseable {
        private static final long serialVersionUID = 1L;
        public final static AtomicBoolean seenClose = new AtomicBoolean();

        @Override
        public void close() {
            seenClose.set(true);          
        }

        @Override
        public Iterable<String> apply(String v) {
            return Collections.singleton(v);
        }      
    }
}
