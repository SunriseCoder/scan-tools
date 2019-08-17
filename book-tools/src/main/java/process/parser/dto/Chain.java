package process.parser.dto;

import java.util.Iterator;

public class Chain<T> implements Iterable<T> {
    private T value;
    private Chain<T> previousChain;
    private Chain<T> nextChain;

    public Chain(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Chain<T> getPreviousChain() {
        return previousChain;
    }

    public void setPreviousChain(Chain<T> previousChain) {
        this.previousChain = previousChain;
    }

    public Chain<T> getNextChain() {
        return nextChain;
    }

    public void setNextChain(Chain<T> nextChain) {
        this.nextChain = nextChain;
    }

    public void linkPrevious(Chain<T> previousChain) {
        this.previousChain = previousChain;
        if (previousChain != null) {
            previousChain.setNextChain(this);
        }
    }

    public void linkNext(Chain<T> nextChain) {
        this.nextChain = nextChain;
        if (nextChain != null) {
            nextChain.setPreviousChain(this);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl<>(this);
    }

    public class IteratorImpl<E> implements Iterator<E> {
        private Chain<E> currentChain;

        public IteratorImpl(Chain<E> chain) {
            currentChain = chain;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = currentChain != null;
            return hasNext;
        }

        @Override
        public E next() {
            E value = currentChain.getValue();
            currentChain = currentChain.getNextChain();
            return value;
        }

    }
}
