package dev;

import Sources.Dictionary;
import Sources.HashedPasswords;
import ThreadProperties.ThreadColor;
import ThreadProperties.ThreadColorFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PasswordHacker {

    private static final ConcurrentLinkedQueue<String> HASHED_PASSWORDS_LIST;
    private static final ConcurrentLinkedQueue<String> FOUND_PASSWORDS = new ConcurrentLinkedQueue<>();
    private static final CopyOnWriteArrayList<String> DICTIONARY;
    private final Lock lock = new ReentrantLock(true);
    private final Condition passwordFound = lock.newCondition();
    private boolean isHacked = false;
    private boolean isEmpty = false;
    private String passwordToDelete;
    private final AtomicInteger producerNumber = new AtomicInteger(0);
    private int numberOfProducerThreads;
    private final ThreadColorFactory threadColorFactory = new ThreadColorFactory();


    static {
        HASHED_PASSWORDS_LIST = HashedPasswords.readHashedPasswords("passwords");
        DICTIONARY = Dictionary.readDictionaryWords("dictionary");
        if (DICTIONARY.isEmpty() || HASHED_PASSWORDS_LIST.isEmpty()) {
            System.out.println("Can't invoke application!");
        }
    }

    private void firstProducerThread() {
        for (String s : DICTIONARY) {
            if (isEmpty) {
                break;
            }
            searchPassword(s);
        }
        producerNumber.incrementAndGet();
        System.out.println(ThreadColor.ANSI_RESET.color() + "First producer finished his work");
    }

    private void secondProducerThread() {
        for (String s : DICTIONARY) {
            if (isEmpty) {
                break;
            }
            searchPassword(s);
            addPrefixSuffixString(s);
        }
        producerNumber.incrementAndGet();
        System.out.println(ThreadColor.ANSI_RESET.color() + "Second producer finished his work");
    }

    private void thirdProducerThread() {
        for (String s : DICTIONARY) {
            if (isEmpty) {
                break;
            }
            for (int i = 0; i < 2; i++) {
                switch (i) {
                    case 0 -> s = s.toUpperCase();
                    case 1 -> {
                        s = s.toLowerCase();
                        s = s.replace(s.charAt(0), Character.toUpperCase(s.charAt(0)));
                    }
                }
                searchPassword(s);
                addPrefixSuffixString(s);
            }
        }
        producerNumber.incrementAndGet();
        System.out.println(ThreadColor.ANSI_RESET.color() + "Third producer finished his work");
    }

    private void addPrefixSuffixString(String word) {
        for (int prefix = 0; prefix < 100; prefix++) {
            if (prefix < 10) {
                searchPassword(String.format("%02d", prefix) + word);
                searchPassword(word + String.format("%02d", prefix));
            } else {
                searchPassword(prefix + word);
                searchPassword(word + prefix);
            }
            for (int postfix = 0; postfix < 100; postfix++) {
                String password;
                if (postfix < 10 && prefix < 10) {
                    password = String.format("%02d", prefix) + word + String.format("%02d", postfix);
                } else if (prefix < 10) {
                    password = String.format("%02d", prefix) + word + postfix;
                } else if (postfix < 10) {
                    password = prefix + word + String.format("%02d", postfix);
                } else {
                    password = prefix + word + postfix;
                }
                searchPassword(password);
            }
        }
    }

    private void searchPassword(String password) {
        String hashedPassword = hashGivenPassword(password);
        lock.lock();
        String color = getThreadColor();
        String threadName = Thread.currentThread().getName();
        try {
            while (isHacked) {
                System.out.println(color + threadName + " Waiting [Other Thread found password]");
                passwordFound.await();
            }
            if (HASHED_PASSWORDS_LIST.contains(hashedPassword)) {
                System.out.printf("%sPassword found:[%s] by %s%n", color, password, threadName);
                passwordToDelete = password;
                isHacked = true;
                passwordFound.signal();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void consumerThread() {
        String color = getThreadColor();
        String threadName = Thread.currentThread().getName();
        while (producerNumber.get() < numberOfProducerThreads && !HASHED_PASSWORDS_LIST.isEmpty()) {
            lock.lock();
            try {
                while (!isHacked && producerNumber.get() < numberOfProducerThreads) {
                    if (!passwordFound.await(30, TimeUnit.SECONDS)) {
                        System.out.println(color + "Consumer checks if all producers did their job or password list is empty.");
                    }
                }
                if (isHacked) {
                    FOUND_PASSWORDS.add(passwordToDelete);
                    HASHED_PASSWORDS_LIST.remove(hashGivenPassword(passwordToDelete));
                    System.out.printf("%sPassword deleted:[%s] by %s%n", color, passwordToDelete, threadName);
                    System.out.printf("%sNumber of passwords left: [%d]%n", color, HASHED_PASSWORDS_LIST.size());
                    isHacked = false;
                    passwordFound.signalAll();
                }
                if (HASHED_PASSWORDS_LIST.isEmpty()) {
                    isEmpty = true;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        System.out.printf("%sNumber of passwords hacked: [%s]!%n%s%n", ThreadColor.ANSI_RESET.color(), FOUND_PASSWORDS.size(), FOUND_PASSWORDS);
    }

    private String getThreadColor() {
        ThreadColor threadColor = ThreadColor.valueOf(Thread.currentThread().getName().toUpperCase());
        return threadColor.color();
    }

    private String hashGivenPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void runThreads() {
        runThreads(List.of(this::firstProducerThread, this::secondProducerThread, this::thirdProducerThread));
    }

    public void runThreads(List<Runnable> tasks) {
        numberOfProducerThreads = tasks.size();
        List<Thread> threads = new ArrayList<>();
        ExecutorService executor = Executors.newCachedThreadPool(threadColorFactory);
        threads.add(threadColorFactory.newThread(this::consumerThread));
        for (Runnable task : tasks) {
            threads.add(threadColorFactory.newThread(task));
        }
        for (Thread thread : threads) {
            executor.submit(thread);
        }
        executor.shutdown();
    }
}
