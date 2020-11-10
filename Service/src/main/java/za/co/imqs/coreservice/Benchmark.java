package za.co.imqs.coreservice;


import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Benchmark {
    private static final AutoMap<String,Measure> measures = new AutoMap<>();

    public static Map<String,Measure> get() {
        return measures;
    }

    public interface Measure {
        void sample(long ms, int num);
        void sample(long ms);
        long getTotal();
        long getNumSamples();
        long getAvg();

        void m(Task t);
    };

    public static class SimpleMeasure implements Measure {
        private long total = 0;
        private long numSamples = 0;

        public void sample(long ms, int num) {
            synchronized (this) {
              numSamples+=num;
              total+=ms;
            }
        }
        public void sample(long ms) {
            sample(ms,1);
        }

        @Override
        public long getTotal() {
            return total;
        }

        @Override
        public long getNumSamples() {
            return numSamples;
        }

        public long getAvg() {
            return getNumSamples() == 0 ? 0 : getTotal() / getNumSamples();
        }

        @Override
        public void m(Task t) {
            final long t0 = System.nanoTime();
            t.task();
            sample(Math.abs(System.nanoTime() - t0));
        }

        public String toString() {
            return String.format("avg %s ms (%s, %s)", getAvg()/1000000, getTotal(), getNumSamples());
        }
    }

    private static class AutoMap<K,V extends Measure> extends HashMap<K,V> {
        @Override
        public V get(Object k) {
            return this.computeIfAbsent((K)k, (s)-> (V)new SimpleMeasure());
        }
    }

    public interface Task {
        public void task();
    }
}
