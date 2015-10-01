package org.fastcatsearch.ir.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

/**
 * Created by swsong on 2015. 9. 24..
 */
public class FileBaseHashSetTest {

    private Random random = new Random(System.currentTimeMillis());

    @Before
    public void init() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    @Test
    public void testNew() throws FileNotFoundException {
        File f = new File("/tmp/a");
        int bucketSize = 10;
        int keySize = 3;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);

        set.put("AAA");
        set.put("AAA");
        set.put("BBB");
        set.put("AAA");
        set.put("BBB");
        set.put("BBB");
        set.put("CCC");
        set.put("CCC");
    }

    @Test
    public void testRandom() {
        int LIMIT = 1000000;
        File f = new File("/tmp/random.set");
        int bucketSize = 1000000;
        int keySize = 36;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);
        System.out.println("size1 : " + f.length());
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = makeString();
            set.put(key);
            if(i % 10000 == 0) {
                System.out.println("count = " + i);
            }
        }
        System.out.println((System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
        System.out.println("size2 : " + f.length());
    }

    @Test
    public void testRandomMemory() {
        int LIMIT = 1000000;
        HashSet<String> set = new HashSet();
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = makeString();
            set.add(key);
            if(i % 10000 == 0) {
                System.out.println("count = " + i);
            }
        }
        System.out.println((System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
    }

    private String makeString() {
        //size는 36. ex) 2d515f46-c9b5-4c05-b019-8dfb19e62f85
        String key = UUID.randomUUID().toString();
        return key;
    }

    @Test
    public void test2() {
        File f = new File("/tmp/2.set");
        int bucketSize = 5;
        int keySize = 1;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);
        System.out.println("size1 : " + f.length());
        set.put("a");
        set.put("b");
        set.put("c");
        set.put("d");
        set.put("d");
        set.put("e");
        set.put("f");
        set.put("g");
        set.put("h");
        set.put("h");
        set.put("i");
        set.put("b");
        set.put("a");
        System.out.println("size2 : " + f.length());
    }
}
